package com.example.cwl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class Utility {

	private static final String INPUTS = "inputs";
	private static final String OUTPUTS = "outputs";
	private static final String ID = "id";
	private static final String TYPE = "type";
	private static final String ARRAY = "array";
	private static final String DESCRIPTION = "description";
	private static final int DEPTH_0 = 0;
	private static final int DEPTH_1 = 1;
	private static final int DEPTH_2 = 2;

	private static final String FLOAT = "float";
	private static final String NULL = "null";
	private static final String BOOLEAN = "boolean";
	private static final String INT = "int";
	private static final String DOUBLE = "double";
	private static final String STRING = "string";
	private static final String LABEL = "label";
	private static final String FILE = "file";
	private static final String FORMAT = "format";
	private LinkedHashMap nameSpace;
	private Map cwlFile;

	public Utility(Map cwlFile) {
		this.cwlFile = cwlFile;
		processNameSpace();
	}

	public void processNameSpace() {

		if (cwlFile.containsKey("$namespaces")) {
			nameSpace = (LinkedHashMap) cwlFile.get("$namespaces");
		}

	}

	public HashMap<String, Integer> processInputDepths() {
		return process(cwlFile.get(INPUTS));
	}

	public HashMap<String, Integer> processOutputDepths() {
		return process(cwlFile.get(OUTPUTS));
	}

	public HashMap<String, PortDetail> processInputDetails() {
		return processdetails(cwlFile.get(INPUTS));
	}

	public HashMap<String, PortDetail> processOutputDetails() {
		return processdetails(cwlFile.get(OUTPUTS));
	}

	private HashMap<String, Integer> process(Object inputs) {

		HashMap<String, Integer> result = new HashMap<>();

		if (inputs.getClass() == ArrayList.class) {

			for (Map input : (ArrayList<Map>) inputs) {
				String currentInputId = (String) input.get(ID);

				Object typeConfigurations;
				try {

					typeConfigurations = input.get(TYPE);
					// if type :single argument
					if (typeConfigurations.getClass() == String.class) {

						if (isValidArrayType(typeConfigurations.toString()))
							result.put(currentInputId, DEPTH_1);
						else
							result.put(currentInputId, DEPTH_0);
						// type : defined as another map which contains type:
					} else if (typeConfigurations.getClass() == LinkedHashMap.class) {
						String inputType = (String) ((Map) typeConfigurations).get(TYPE);
						if (inputType.equals(ARRAY)) {
							result.put(currentInputId, DEPTH_1);

						}
					} else if (typeConfigurations.getClass() == ArrayList.class) {
						if (isValidDataType((ArrayList) typeConfigurations)) {
							result.put(currentInputId, DEPTH_0);
						}

					}

				} catch (ClassCastException e) {

					System.out.println("Class cast exception !!!");
				}

			}
		} else if (inputs.getClass() == LinkedHashMap.class) {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode inputs_json = mapper.valueToTree(inputs);
			Iterator<Entry<String, JsonNode>> iterator = inputs_json.fields();

			while (iterator.hasNext()) {
				Entry<String, JsonNode> entry = iterator.next();
				String currentInputId = entry.getKey();
				JsonNode typeConfigurations = entry.getValue();

				if (typeConfigurations.getClass() == TextNode.class) {
					if (typeConfigurations.asText().startsWith("$")) {
						System.out.println("Exception");
					}
					// inputs:
					// input_1: int[]
					else if (isValidArrayType(typeConfigurations.asText()))
						result.put(currentInputId, DEPTH_1);
					// inputs:
					// input_1: int
					else
						result.put(currentInputId, DEPTH_0);

				} else if (typeConfigurations.getClass() == ObjectNode.class) {

					if (typeConfigurations.has(TYPE)) {
						JsonNode inputType = typeConfigurations.get(TYPE);
						// inputs:
						// input_1:
						// type: [int,"null"]
						if (inputType.getClass() == ArrayNode.class) {
							try {
								if (isValidDataType(mapper.treeToValue(inputType, List.class)))
									result.put(currentInputId, DEPTH_0);
							} catch (JsonProcessingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							// inputs:
							// input_1:
							// type: array or int[]
							if (inputType.asText().equals(ARRAY) || isValidArrayType(inputType.asText()))
								result.put(currentInputId, DEPTH_1);
							// inputs:
							// input_1:
							// type: int or int?
							else
								result.put(currentInputId, DEPTH_0);
						}
					}
				}
			}
		}
		return result;
	}

	public boolean isValidArrayType(String type) {
		Pattern pattern = Pattern.compile("\\[\\]$");
		Matcher matcher = pattern.matcher(type);
		if (matcher.find() && isValidDataType(Arrays.asList(type.split("\\[\\]")[0])))
			return true;
		else
			return false;
	}

	private HashMap<String, PortDetail> processdetails(Object inputs) {

		HashMap<String, PortDetail> result = new HashMap<>();

		if (inputs.getClass() == ArrayList.class) {

			for (Map input : (ArrayList<Map>) inputs) {
				PortDetail detail = new PortDetail();
				String currentInputId = (String) input.get(ID);

				getParamDetails(result, input, detail, currentInputId);

			}
		} else if (inputs.getClass() == LinkedHashMap.class) {
			
			ObjectMapper mapper = new ObjectMapper();
			JsonNode inputs_json = mapper.valueToTree(inputs);
			Iterator<Entry<String, JsonNode>> iterator = inputs_json.fields();
			
			while (iterator.hasNext()) {
				PortDetail detail = new PortDetail();
				Entry<String, JsonNode> s = iterator.next();
				try {
					if (s.getValue().getClass() == ObjectNode.class)
						getParamDetails(result, mapper.treeToValue(s.getValue(), LinkedHashMap.class), detail,
								s.getKey());
					
					else
						getParamDetails(result, mapper.treeToValue(s.getValue(), String.class), detail, s.getKey());
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	private void getParamDetails(HashMap<String, PortDetail> result, Object input, PortDetail detail,
			String currentInputId) {
		if (input.getClass() == LinkedHashMap.class) {
			extractDescription((Map) input, detail);

			extractFormat((Map) input, detail);

			extractLabel((Map) input, detail);
		}
		result.put(currentInputId, detail);
	}

	private void extractLabel(Map input, PortDetail detail) {
		if (input != null)
			if (input.containsKey(LABEL)) {
				detail.setLabel((String) input.get(LABEL));
			} else {
				detail.setLabel(null);
			}
	}

	private void extractDescription(Map input, PortDetail detail) {
		if (input != null)
			if (input.containsKey(DESCRIPTION)) {
				detail.setDescription((String) input.get(DESCRIPTION));
			} else {
				detail.setDescription(null);
			}
	}

	private void extractFormat(Map input, PortDetail detail) {
		if (input != null)
			if (input.containsKey(FORMAT)) {

				Object formatInfo = input.get(FORMAT);

				ArrayList<String> format = new ArrayList<>();
				detail.setFormat(format);

				if (formatInfo.getClass() == String.class) {

					extractThisFormat(formatInfo.toString(), detail);
				} else if (formatInfo.getClass() == ArrayList.class) {
					for (Object eachFormat : (ArrayList) formatInfo) {
						extractThisFormat(eachFormat.toString(), detail);
					}
				}

			}
	}

	private void extractThisFormat(String formatInfoString, PortDetail detail) {
		if (formatInfoString.startsWith("$")) {

			detail.addFormat(formatInfoString);
		} else if (formatInfoString.contains(":")) {
			String format[] = formatInfoString.split(":");
			String namespaceKey = format[0];
			String urlAppednd = format[1];
			if (!nameSpace.isEmpty()) {
				if (nameSpace.containsKey(namespaceKey))
					detail.addFormat(nameSpace.get(namespaceKey) + urlAppednd);
				else

					detail.addFormat(formatInfoString);
			} else {
				detail.addFormat(formatInfoString);
			}
		} else {
			detail.addFormat(formatInfoString);
		}
	}

	public boolean isValidDataType(List typeConfigurations) {
		for (Object type : typeConfigurations) {
			if (!(((String) type).equals(FLOAT) || ((String) type).equals(NULL) || ((String) type).equals(BOOLEAN)
					|| ((String) type).equals(INT) || ((String) type).equals(STRING) || ((String) type).equals(DOUBLE)
					|| ((String) type).equals(FILE)))
				return false;
		}
		return true;
	}
}

package com.example.exampletool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

public class ExampleActivity extends AbstractAsynchronousActivity<ExampleActivityConfigurationBean>
		implements AsynchronousActivity<ExampleActivityConfigurationBean> {

	/*
	 * Best practice: Keep port names as constants to avoid misspelling. This
	 * would not apply if port names are looked up dynamically from the service
	 * operation, like done for WSDL services.
	 */
	private static final String IN_FIRST_INPUT = "firstInput";
	private static final String IN_EXTRA_DATA = "extraData";
	private static final String OUT_MORE_OUTPUTS = "moreOutputs";
	private static final String OUT_SIMPLE_OUTPUT = "simpleOutput";
	private static final String OUT_REPORT = "report";
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
	private static final String LABEL = "label";
	private static final String FORMAT = "format";
	private HashMap<String, PortDetail> processedInputs;

	private HashMap<String, PortDetail> processedOutputs;
	private ExampleActivityConfigurationBean configBean;
	private LinkedHashMap nameSpace;

	@Override
	public void configure(ExampleActivityConfigurationBean configBean) throws ActivityConfigurationException {

		// Any pre-config sanity checks
		if (configBean.getExampleString().equals("invalidExample")) {
			throw new ActivityConfigurationException("Example string can't be 'invalidExample'");
		}
		// Store for getConfiguration(), but you could also make
		// getConfiguration() return a new bean from other sources
		this.configBean = configBean;

		// OPTIONAL:
		// Do any server-side lookups and configuration, like resolving WSDLs

		// myClient = new MyClient(configBean.getExampleUri());
		// this.service = myClient.getService(configBean.getExampleString());

		// REQUIRED: (Re)create input/output ports depending on configuration
		configurePorts();
	}

	public void processNameSpace(Map cwlFile) {

		if (cwlFile.containsKey("$namespaces")) {
			nameSpace = (LinkedHashMap) cwlFile.get("$namespaces");
		}

	}

	protected void configurePorts() {
		// In case we are being reconfigured - remove existing ports first
		// to avoid duplicates
		removeInputs();
		removeOutputs();
		Map cwlFile = configBean.getCwlConfigurations();
		processNameSpace(cwlFile);
		if (cwlFile != null) {
			processedInputs = processInputs(cwlFile);

			for (String inputId : processedInputs.keySet()) {
				int depth = processedInputs.get(inputId).getDepth();
				if (depth == DEPTH_0)
					addInput(inputId, DEPTH_0, true, null, String.class);
				else if (depth == DEPTH_1)
					addInput(inputId, DEPTH_1, true, null, byte[].class);

			}
			processedOutputs = processOutputs(cwlFile);
			for (String inputId : processedOutputs.keySet()) {
				int depth = processedOutputs.get(inputId).getDepth();
				if (depth == DEPTH_0)
					addOutput(inputId, DEPTH_0);
				else if (depth == DEPTH_1)
					addOutput(inputId, DEPTH_1);

			}
		}

	}

	private HashMap<String, PortDetail> processOutputs(Map cwlFile) {
		return process(cwlFile.get(OUTPUTS));
	}

	private HashMap<String, PortDetail> processInputs(Map cwlFile) {
		return process(cwlFile.get(INPUTS));
	}

	private HashMap<String, PortDetail> process(Object inputs) {

		HashMap<String, PortDetail> result = new HashMap<>();

		if (inputs.getClass() == ArrayList.class) {

			for (Map input : (ArrayList<Map>) inputs) {
				PortDetail detail = new PortDetail();
				String currentInputId = (String) input.get(ID);

				extractDescription(input, detail);

				extractFormat(input, detail);

				extractLabel(input, detail);

				Object typeConfigurations;
				try {

					typeConfigurations = input.get(TYPE);
					// if type :single argument
					if (typeConfigurations.getClass() == String.class) {
						detail.setDepth(DEPTH_0);

						result.put(currentInputId, detail);
						// type : defined as another map which contains type:
					} else if (typeConfigurations.getClass() == LinkedHashMap.class) {
						String inputType = (String) ((Map) typeConfigurations).get(TYPE);
						if (inputType.equals(ARRAY)) {
							detail.setDepth(DEPTH_1);
							result.put(currentInputId, detail);

						}
					} else if (typeConfigurations.getClass() == ArrayList.class) {
						if (isValidDataType((ArrayList) typeConfigurations)) {
							detail.setDepth(DEPTH_0);
							result.put(currentInputId, detail);
						}

					}

				} catch (ClassCastException e) {

					System.out.println("Class cast exception !!!");
				}

			}
		} else if (inputs.getClass() == LinkedHashMap.class) {
			for (Object parameter : ((Map) inputs).keySet()) {
				if (parameter.toString().startsWith("$"))
					System.out.println("Exception");
			}
		}
		return result;
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

	@SuppressWarnings("unchecked")
	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs, final AsynchronousActivityCallback callback) {
		// Don't execute service directly now, request to be run ask to be run
		// from thread pool and return asynchronously
		callback.requestRun(new Runnable() {

			public void run() {
				InvocationContext context = callback.getContext();
				ReferenceService referenceService = context.getReferenceService();
				// Resolve inputs
				String firstInput = (String) referenceService.renderIdentifier(inputs.get(IN_FIRST_INPUT), String.class,
						context);

				// Support our configuration-dependendent input
				boolean optionalPorts = configBean.getExampleString().equals("specialCase");

				List<byte[]> special = null;
				// We'll also allow IN_EXTRA_DATA to be optionally not provided
				if (optionalPorts && inputs.containsKey(IN_EXTRA_DATA)) {
					// Resolve as a list of byte[]
					special = (List<byte[]>) referenceService.renderIdentifier(inputs.get(IN_EXTRA_DATA), byte[].class,
							context);
				}

				// TODO: Do the actual service invocation
				// try {
				// results = this.service.invoke(firstInput, special)
				// } catch (ServiceException ex) {
				// callback.fail("Could not invoke Example service " +
				// configBean.getExampleUri(),
				// ex);
				// // Make sure we don't call callback.receiveResult later
				// return;
				// }

				// Register outputs
				Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
				String simpleValue = "simple";
				T2Reference simpleRef = referenceService.register(simpleValue, 0, true, context);
				outputs.put(OUT_SIMPLE_OUTPUT, simpleRef);

				// For list outputs, only need to register the top level list
				List<String> moreValues = new ArrayList<String>();
				moreValues.add("Value 1");
				moreValues.add("Value 2");
				T2Reference moreRef = referenceService.register(moreValues, 1, true, context);
				outputs.put(OUT_MORE_OUTPUTS, moreRef);

				if (optionalPorts) {
					// Populate our optional output port
					// NOTE: Need to return output values for all defined output
					// ports
					String report = "Everything OK";
					outputs.put(OUT_REPORT, referenceService.register(report, 0, true, context));
				}

				// return map of output data, with empty index array as this is
				// the only and final result (this index parameter is used if
				// pipelining output)
				callback.receiveResult(outputs, new int[0]);
			}
		});
	}

	public boolean isValidDataType(ArrayList typeConfigurations) {
		for (Object type : typeConfigurations) {
			if (!(((String) type).equals(FLOAT) || ((String) type).equals(NULL)))
				return false;
		}
		return true;
	}

	@Override
	public ExampleActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

	public HashMap<String, PortDetail> getProcessedInputs() {
		return processedInputs;
	}

	public void setProcessedInputs(HashMap<String, PortDetail> processedInputs) {
		this.processedInputs = processedInputs;
	}

	public HashMap<String, PortDetail> getProcessedOutputs() {
		return processedOutputs;
	}

	public void setProcessedOutputs(HashMap<String, PortDetail> processedOutputs) {
		this.processedOutputs = processedOutputs;
	}

}

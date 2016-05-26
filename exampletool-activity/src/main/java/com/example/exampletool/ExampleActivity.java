package com.example.exampletool;

import java.util.ArrayList;
import java.util.HashMap;
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
	private static final String ID = "id";
	private static final String TYPE = "type";
	private static final String ARRAY = "array";
	private static final String FILE = "File";
	private static final String ITEMS = "items";

	private ExampleActivityConfigurationBean configBean;

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

	protected void configurePorts() {
		// In case we are being reconfigured - remove existing ports first
		// to avoid duplicates
		removeInputs();
		removeOutputs();
		Map cwlFile = configBean.getCwlConfigurations();

		// Get all input objects in
		ArrayList<Map> inputs = (ArrayList<Map>) cwlFile.get(INPUTS);

		HashMap<String, Type> processedInputs;

		if (inputs != null) {
			processedInputs = processInputs(inputs);

			for (String inputId : processedInputs.keySet()) {
				if (processedInputs.get(inputId).getType().equals(FILE))
					addInput(inputId, 0, true, null, String.class);
				if (processedInputs.get(inputId).getType().equals(ARRAY))
					addInput(inputId, 1, true, null, byte[].class);
			}

		}

	}

	private HashMap<String, Type> processInputs(ArrayList<Map> inputs) {

		HashMap<String, Type> result = new HashMap<>();

		for (Map input : inputs) {

			String Id = (String) input.get(ID);
			// This require for nested type definitions
			Map typeConfigurations;
			// this object holds the type and if it's an array then type of the
			// elements in the array
			Type type = new Type();
			try {
				/*
				 * This part will go through nested type definitions
				 * 
				 * type : type : array items : boolean
				 * 
				 */

				typeConfigurations = (Map) input.get(TYPE);
				type.setType((String) typeConfigurations.get(TYPE));
				type.setItems((String) typeConfigurations.get(ITEMS));
			} catch (ClassCastException e) {
				/*
				 * This exception means type is described as single argument ex:
				 * type : File
				 */
				type.setType((String) input.get(TYPE));
				type.setItems(null);
			}
			result.put(Id, type);
		}
		return result;
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

	@Override
	public ExampleActivityConfigurationBean getConfiguration() {
		return this.configBean;
	}

}

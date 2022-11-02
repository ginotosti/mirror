package astri.eietest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.google.common.collect.ImmutableList;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.model.nodes.objects.ServerTypeNode;
import org.eclipse.milo.opcua.sdk.client.model.nodes.variables.ServerStatusTypeNode;
import org.eclipse.milo.opcua.sdk.client.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedDataItem;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription;
import org.eclipse.milo.opcua.sdk.client.subscriptions.ManagedSubscription.ChangeListener;
import org.eclipse.milo.opcua.stack.client.DiscoveryClient;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.ServerState;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.ServerStatusDataType;
import org.eclipse.milo.opcua.stack.core.util.EndpointUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiloClient {

    OpcUaClient client;
    // private String opcuuaUrl="opc.tcp://milo.digitalpetri.com:62541/milo";
    private String opcuuaUrl = "opc.tcp://tcu1.mavpn.org:4840";
    private String host;
    private int port;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private CompletableFuture<OpcUaClient> future;
    List<EndpointDescription> endpoints;

    public MiloClient() throws UaException, InterruptedException, ExecutionException {
        this.endpoints = DiscoveryClient.getEndpoints(opcuuaUrl).get();
        /*
         * for (EndpointDescription endpointDescription : endpoints) {
         * System.out.println(endpointDescription.getSecurityMode());
         * 
         * }
         */
        createClient();
    }

    public MiloClient(String host, int port, String path) throws UaException, InterruptedException, ExecutionException {
        this.port = port;
        this.host = host;
        this.opcuuaUrl = "opc.tcp://" + host + ":" + port + path;
        this.endpoints = DiscoveryClient.getEndpoints(opcuuaUrl).get();
        /*
         * for (EndpointDescription endpointDescription : endpoints) {
         * System.out.println(endpointDescription.getSecurityMode());
         * 
         * }
         */
        createClient();
    }

    public void createClient() throws UaException, InterruptedException, ExecutionException {
        OpcUaClientConfigBuilder cfg = new OpcUaClientConfigBuilder();
        Optional<EndpointDescription> found = endpoints.stream()
                .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getUri())) //
                .findFirst();
        if (found.isPresent()) {
            EndpointDescription configPoint = EndpointUtil.updateUrl(found.get(), host, port);
            cfg.setEndpoint(configPoint);
            this.client = OpcUaClient.create(cfg.build());
            client.connect().get();
        } else {
            logger.info("NO ENDPOINTS OF INTEREST");
            new RuntimeException("no desired endpoint could be selected");
            System.exit(1);
        }

    }

    public void connect() throws InterruptedException, ExecutionException {
        client.connect().get();
        System.out.println(host);
    }

    public void disconnect() throws InterruptedException, ExecutionException {
        client.disconnect().get();
    }

    private CompletableFuture<List<DataValue>> readServerStateAndTime(OpcUaClient client) {
        System.out.println(host);
        List<NodeId> nodeIds = ImmutableList.of(
                Identifiers.Server_ServerStatus_State,
                Identifiers.Server_ServerStatus_CurrentTime);

        return client.readValues(0.0, TimestampsToReturn.Both, nodeIds);
    }

    public void testReadAsync() {
        readServerStateAndTime(client).thenAccept(values -> {
            DataValue v0 = values.get(0);
            DataValue v1 = values.get(1);

            logger.info("State={}", ServerState.from((Integer) v0.getValue().getValue()));
            logger.info("CurrentTime={}", v1.getValue().getValue());
            future.complete(client);
        });
    }

    public DataValue testReadSync(NodeId rnode) throws UaException {
        UaVariableNode node = client.getAddressSpace().getVariableNode(rnode);
        DataValue value = node.readValue();
        logger.info("StartTime={}", value.getValue().getValue());
        return value;
    }

    public DataValue readNode(NodeId node) throws UaException, InterruptedException, ExecutionException {
        logger.info("leggo");
        UaVariableNode testNode = (UaVariableNode) client.getAddressSpace().getNode(node);
        DataValue value = null;
        // Read the Value attribute
        CompletableFuture<DataValue> result = client.readValue(0.0, TimestampsToReturn.Both, node);
        value = result.get();
        //QualifiedName browseName = testNode.readBrowseName();

        //System.out.println(browseName.getName());
        //System.out.println("Value:" + value.getValue().getValue());
        // Read the BrowseName attribute

        // Read the Description attribute, with timestamps and quality intact
        //DataValue descriptionValue = testNode.readAttribute(AttributeId.Description);
        return value;
    }

    public void writeValue(NodeId nodeId, double value) throws InterruptedException, ExecutionException {
        writeValue(nodeId, (Object)value);
    }
    public void writeValue(NodeId nodeId, Object value) throws InterruptedException, ExecutionException {
        System.out.format("Write Obj:"+nodeId+" " +value);
        Variant v=new Variant(value);
        DataValue val=new DataValue(v,null, null);
        write(nodeId, val).whenComplete((result, error) -> {
            if (error == null) {
                System.out.format("Result: %s%n", result);

            } else {
                error.printStackTrace();
            }
        }).get();
    }

    public void writeValue(NodeId nodeId, short value) throws InterruptedException, ExecutionException {
        writeValue(nodeId, (Object)value);
    }

    private CompletableFuture<StatusCode> write(NodeId nodeId, DataValue value) {
        return client.writeValue(nodeId, value);
    }

    public void getServerInfo() throws UaException {
        ServerTypeNode serverNode = (ServerTypeNode) client.getAddressSpace().getObjectNode(
                Identifiers.Server,
                Identifiers.ServerType);

        // Read properties of the Server object...
        String[] serverArray = serverNode.getServerArray();
        String[] namespaceArray = serverNode.getNamespaceArray();

        logger.info("ServerArray={}", Arrays.toString(serverArray));
        logger.info("NamespaceArray={}", Arrays.toString(namespaceArray));

        // Read the value of attribute the ServerStatus variable component
        ServerStatusDataType serverStatus = serverNode.getServerStatus();

        logger.info("ServerStatus={}", serverStatus);

        // Get a typed reference to the ServerStatus variable
        // component and read value attributes individually
        ServerStatusTypeNode serverStatusNode = serverNode.getServerStatusNode();
        BuildInfo buildInfo = serverStatusNode.getBuildInfo();
        DateTime startTime = serverStatusNode.getStartTime();
        DateTime currentTime = serverStatusNode.getCurrentTime();
        ServerState state = serverStatusNode.getState();

        logger.info("ServerStatus.BuildInfo={}", buildInfo);
        logger.info("ServerStatus.StartTime={}", startTime);
        logger.info("ServerStatus.CurrentTime={}", currentTime);
        logger.info("ServerStatus.State={}", state);

        // future.complete(client);
    }

    public void createSubscription(NodeId node) throws InterruptedException, ExecutionException {

        UaSubscription subscription = client.getSubscriptionManager().createSubscription(1000.0).get();
        // subscribe to the Value attribute of the server's CurrentTime node
        ReadValueId readValueId = new ReadValueId(
                node,
                AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE);
        UInteger clientHandle = subscription.nextClientHandle();
        UInteger size = UInteger.valueOf(10);
        MonitoringParameters parameters = new MonitoringParameters(
                clientHandle,
                1000.0, // sampling interval
                null, // filter, null means use default
                size, // queue size
                true // discard oldest
        );
        List<MonitoredItemCreateRequest> allRequests = new ArrayList<>();

        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(
                readValueId,
                MonitoringMode.Reporting,
                parameters);
        allRequests.add(request);
        // when creating items in MonitoringMode.Reporting this callback is where each
        // item needs to have its
        // value/event consumer hooked up. The alternative is to create the item in
        // sampling mode, hook up the
        // consumer after the creation call completes, and then change the mode for all
        // items to reporting.
        UaSubscription.ItemCreationCallback onItemCreated = (item, id) -> item
                .setValueConsumer(this::onSubscriptionValue);

        List<UaMonitoredItem> items = subscription.createMonitoredItems(
                TimestampsToReturn.Both,
                allRequests,
                onItemCreated).get();

        for (UaMonitoredItem item : items) {
            if (item.getStatusCode().isGood()) {
                logger.info("item created for nodeId={}", item.getReadValueId().getNodeId());
            } else {
                logger.warn(
                        "failed to create item for nodeId={} (status={})",
                        item.getReadValueId().getNodeId(), item.getStatusCode());
            }
        }

    }

    private void onSubscriptionValue(UaMonitoredItem item, DataValue value) {
        logger.info(
                "subscription value received:" +
                        item.getReadValueId().getNodeId() + value.getValue());
    }

    public void createManagedSubscription(NodeId node, ChangeListener listener) throws UaException {
        ManagedSubscription subscription = ManagedSubscription.create(client, 1000.0);
        subscription.addChangeListener(listener);
        ManagedDataItem dataItem = subscription.createDataItem(node);
        if (!dataItem.getStatusCode().isGood()) {
            throw new RuntimeException("Unable to create subscription");
        }
    }

    public void createManagedSubscription(List<NodeId> nodes, ChangeListener listener) throws UaException {
        ManagedSubscription subscription = ManagedSubscription.create(client, 1000.0);
        subscription.addChangeListener(listener);
        for (NodeId node : nodes) {
            ManagedDataItem dataItem = subscription.createDataItem(node);
            logger.info("subscribe to:"+node.toParseableString());
            if (!dataItem.getStatusCode().isGood()) {
                throw new RuntimeException("Unable to create subscription");
            }
        }
    }

}

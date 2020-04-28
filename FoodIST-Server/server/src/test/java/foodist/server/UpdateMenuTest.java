package foodist.server;

import foodist.server.data.Menu;
import foodist.server.grpc.contract.Contract;
import foodist.server.grpc.contract.FoodISTServerServiceGrpc;
import foodist.server.service.ServiceImplementation;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class UpdateMenuTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private ServiceImplementation impl;
    private FoodISTServerServiceGrpc.FoodISTServerServiceBlockingStub stub;


    private static final String NAME = "NAME";
    private static final double PRICE = 2.0d;
    private static final double DELTA = 0.01d;
    private static final String LANGUAGE = "pt";
    private static final String SERVICE = "SERVICE";
    private static final long MENU_ID = 0;

    private static Contract.AddMenuRequest request;

    private static Contract.UpdateMenuRequest updateRequest;
    private static Contract.UpdateMenuRequest invalidUpdateRequest;

    @BeforeClass
    public static void oneTimeSetup() {
        request = Contract.AddMenuRequest.newBuilder()
                .setName(NAME)
                .setPrice(PRICE)
                .setLanguage(LANGUAGE)
                .setFoodService(SERVICE)
                .setType(Contract.FoodType.Meat)
                .build();

        updateRequest = Contract.UpdateMenuRequest.newBuilder()
                .setMenuId(MENU_ID)
                .build();

        invalidUpdateRequest = Contract.UpdateMenuRequest.newBuilder()
                .setMenuId(MENU_ID + 1)
                .build();
    }


    @Before
    public void setup() throws IOException {
        String serverName = InProcessServerBuilder.generateName();

        impl = new ServiceImplementation();

        grpcCleanup.register(InProcessServerBuilder.forName(serverName).directExecutor().addService(impl).build().start());
        ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build());

        this.stub = FoodISTServerServiceGrpc.newBlockingStub(channel);
        stub.addMenu(request);
    }

    @After
    public void teardown() {
        Menu.resetCounter();
    }

    @Test
    public void validTest() {
        Contract.PhotoReply reply = stub.updateMenu(updateRequest);
        assertEquals(reply.getPhotoIDCount(), 0);
    }

    @Test
    public void invalidIdTest() {
        exceptionRule.expect(StatusRuntimeException.class);
        try {
            stub.updateMenu(invalidUpdateRequest);
        } catch (StatusRuntimeException e) {
            assertEquals(e.getStatus(), Status.NOT_FOUND);
            throw e;
        }
    }
}
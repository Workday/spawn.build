package build.spawn.application.local;

import build.base.network.Network;
import build.base.table.Table;
import build.spawn.application.Machine;
import build.spawn.platform.local.LocalMachine;
import org.junit.jupiter.api.Test;

import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Enumeration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LocalMachine}s.
 *
 * @author brian.oliver
 * @since Oct-2018
 */
public class LocalMachineTests
    implements MachineComplianceTests {

    @Override
    public Machine machine() {
        return LocalMachine.get();
    }

    /**
     * Ensure the {@link LocalMachine} network can be discovered.
     *
     * @throws Exception should a network exception occur
     */
    @Test
    public void shouldDiscoverLocalNetwork()
        throws Exception {

        final Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();

        while (enumeration.hasMoreElements()) {
            final NetworkInterface networkInterface = enumeration.nextElement();

            final Table table = Table.create();
            table.addRow("Interface", networkInterface.getName());
            table.addRow("Display Name", networkInterface.getDisplayName());
            table.addRow("Hardware Address", Arrays.toString(networkInterface.getHardwareAddress()));
            table.addRow("MTU", Integer.toString(networkInterface.getMTU()));
            table.addRow("Is Loopback?", Boolean.toString(networkInterface.isLoopback()));
            table.addRow("Is Point To Point?", Boolean.toString(networkInterface.isPointToPoint()));
            table.addRow("Is Virtual?", Boolean.toString(networkInterface.isVirtual()));

            // output the InetAddresses for each interface (together with reachability)
            final Table interfaceAddresses = Table.create();
            networkInterface.getInterfaceAddresses().forEach(
                interfaceAddress -> interfaceAddresses.addRow(interfaceAddress.getAddress().toString()
                    + (Network.isReachable(interfaceAddress.getAddress())
                    ? ""
                    : " (unreachable)")));
            table.addRow("Interface Addresses", interfaceAddresses.toString());

            System.out.println(table);
        }

        assertThat(LocalMachine.get().addresses())
            .isNotEmpty();
    }
}

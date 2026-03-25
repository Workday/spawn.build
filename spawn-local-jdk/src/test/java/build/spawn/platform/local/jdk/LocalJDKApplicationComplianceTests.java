package build.spawn.platform.local.jdk;

import build.spawn.application.Machine;
import build.spawn.platform.local.LocalMachine;

/**
 * {@link LocalMachine}-based {@link MachineAgnosticJDKApplicationComplianceTests}.
 *
 * @author brian.oliver
 * @since Jan-2025
 */
public class LocalJDKApplicationComplianceTests
    implements MachineAgnosticJDKApplicationComplianceTests {

    @Override
    public Machine machine() {
        return LocalMachine.get();
    }
}

package devicedriver

import spock.lang.Specification

class DeviceDriverTest extends Specification {

    def "read from hardware"() {
        given:
        FlashMemoryDevice hardware = Mock()
        def driver = new DeviceDriver(hardware)

        when:
        def data = driver.read(0xFF)

        then:
        data == (byte)0
    }

}

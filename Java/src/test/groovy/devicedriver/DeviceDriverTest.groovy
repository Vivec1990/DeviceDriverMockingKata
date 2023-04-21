package devicedriver


import spock.lang.Specification

class DeviceDriverTest extends Specification {

    def INIT_ADDRESS = 0x00
    def PROGRAM_COMMAND = 0x40

    def address = 1000
    def data = 0x22

    DeviceClock clock = Mock()
    FlashMemoryDevice hardware = Mock()

    def "read from hardware"() {
        given:

        def driver = new DeviceDriver(hardware, clock)

        when:
        def data = driver.read(0xFF)

        then:
        1 * hardware.read(0xFF) >> 50
        data == (byte) 50
    }

    def "write to vpp protected range"() {
        given:
        FlashMemoryDevice hardware = Mock()
        def driver = new DeviceDriver(hardware, clock)

        when:
        driver.write(address, (byte) data)

        then:
        1 * hardware.write(INIT_ADDRESS, PROGRAM_COMMAND)
        1 * hardware.write(address, data)
        1 * hardware.read(INIT_ADDRESS) >> 0x20
        thrown VppException
    }

    def "write encounters an internal error"() {
        given:
        FlashMemoryDevice hardware = Mock()
        def driver = new DeviceDriver(hardware, clock)

        when:
        driver.write(address, (byte) data)

        then:
        1 * hardware.write(INIT_ADDRESS, PROGRAM_COMMAND)
        1 * hardware.write(address, data)
        1 * hardware.read(INIT_ADDRESS) >> 0x10
        thrown InternalErrorException
    }

    def "write to protected block"() {
        given:
        FlashMemoryDevice hardware = Mock()
        def driver = new DeviceDriver(hardware, clock)

        when:
        driver.write(address, (byte) data)

        then:
        1 * hardware.write(INIT_ADDRESS, PROGRAM_COMMAND)
        1 * hardware.write(address, data)
        1 * hardware.read(INIT_ADDRESS) >> 0x08
        thrown ProtectedBlockException
    }

    def "write encounters read failure"() {
        given:
        FlashMemoryDevice hardware = Mock()
        def driver = new DeviceDriver(hardware, clock)

        when:
        driver.write(address, (byte) data)

        then:
        1 * hardware.write(INIT_ADDRESS, PROGRAM_COMMAND)
        1 * hardware.write(address, data)
        1 * hardware.read(0x00) >> 0x02
        1 * hardware.read(address) >> _
        thrown ReadFailureException
    }

    def "write has a timout"() {
        given:
        FlashMemoryDevice hardware = Mock()
        def driver = new DeviceDriver(hardware, clock)

        when:
        driver.write(address, (byte) data)

        then:
        1 * hardware.write(INIT_ADDRESS, PROGRAM_COMMAND)
        1 * hardware.write(address, data)
        1 * hardware.read(INIT_ADDRESS) >> 0x00
        2 * clock.nanoTime() >> 0 >> 200_000_000
        thrown TimeoutException
    }

    def "write is successful"() {
        given:
        FlashMemoryDevice hardware = Mock()
        def driver = new DeviceDriver(hardware, clock)

        when:
        driver.write(address, (byte) data)

        then:
        1 * hardware.write(INIT_ADDRESS, PROGRAM_COMMAND)
        1 * hardware.write(address, data)
        1 * hardware.read(INIT_ADDRESS) >> 0x02
        1 * hardware.read(address) >> data
    }

}

def read_modem(reader):
    ret     = ''
    timeout = 2

    while timeout > 0:
        byte, saw = reader.read(255)

        if byte > 0:
            ret += saw
        else:
            timeout -= 1

    return ret
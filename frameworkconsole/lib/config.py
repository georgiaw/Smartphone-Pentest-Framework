import ConfigParser

class Config():
    def __init__(self, config_file):
        self.config = ConfigParser.RawConfigParser()
        self.config.readfp(FakeSecHead(open(config_file)))

    def get(self, key):
        return self.config.get('asection', key);

class FakeSecHead(object):
    def __init__(self, fp):
        self.fp = fp
        self.sechead = '[asection]\n'
    def readline(self):
        if self.sechead:
          try: return self.sechead
          finally: self.sechead = None
        else: return self.fp.readline()

class DBException(Exception):
    pass

class MySQL():
    def __init__(self, db_name, host, port, username, password):
        import MySQLdb
        try:
            self.conn = MySQLdb.connect(db=db_name, host=host, port=int(port), user=username, passwd=password)
        except MySQLdb.OperationalError as e:
            if e.args[0] ==1049:
                raise DBException(2, e.message)
            else:
                raise

        self.cur = self.conn.cursor()

    def query(self, q, params=()):
        self.cur.execute(q, params)
        self.conn.commit()

    def fetchone(self):
        return self.cur.fetchone()

    def fetchall(self):
        return self.cur.fetchall()

    def close():        
        self.cur.close()
        self.conn.close()

class Postgre():
    def __init__(self, db_name, host, port, username, password):
        import psycopg2

        try:
            self.conn = psycopg2.connect("dbname=%s host=%s port=%s user=%s password=%s"%(db_name, host, port, username, password))
        except psycopg2.OperationalError as e:
            if '"%s" does not exist'%db_name in e.message:
                raise DBException(2, e.message)
            else:
                raise

        self.cur = self.conn.cursor()

    def query(self, q, params=()):
        self.cur.execute(q, params)
        self.conn.commit()

    def fetchone(self):
        return self.cur.fetchone()

    def fetchall(self):
        return self.cur.fetchall()

    def close():        
        self.cur.close()
        self.conn.close()

class DB():
    def __init__(self, dbtype=None, db_name='framework', host=None, port=None, username=None, password=None, config=None):
        dbtype = dbtype or (config.get('DATABASETYPE') if config else None)

        if 'postgre' in dbtype.lower():
            self._dbe = Postgre( \
                db_name, \
                host or config.get('MYSQLSERVER'), \
                port or config.get('MYSQLPORT'), \
                username or config.get('MYSQLUSER'), \
                password or (config.get('MYSQLPASS') if config else None), \
            )
        elif 'mysql' in dbtype.lower():
            self._dbe = MySQL( \
                db_name, \
                host or config.get('MYSQLSERVER'), \
                port or config.get('MYSQLPORT'), \
                username or config.get('MYSQLUSER'), \
                password or (config.get('MYSQLPASS') if config else None), \
            )
        else:
            raise DBException(1, "Unsupported DB")

    def query(self, q, params=()):
        return self._dbe.query(q, params)

    def fetchone(self):
        r = self._dbe.fetchone()
        return r if r else ['']

    def fetchall(self):
        return self._dbe.fetchall()

    def close():        
        return self._dbe.close()

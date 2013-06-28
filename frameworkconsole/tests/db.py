import os,sys
sys.path.insert(0,os.path.dirname(os.path.abspath(__file__)))

import unittest
from lib.db import DB

class DBTestSequenceFunctions(unittest.TestCase):
    def test_postgre(self):
        self.queries(DB('postgre', 'framework', 'localhost', 5432, 'myname', '123456'))

    def test_mysql(self):
        self.queries(DB('mysql', 'framework', 'localhost', 3306, 'root', '123456'))
    
    def queries(self, db):
        db.query('drop table test')
        db.query('create table if not exists test(first varchar(80), second int);')

        db.query("insert into test (first, second) values(%s, %s)", ('first value', 123))
        db.query("insert into test (first, second) values(%s, %s)", ('second value', 456))
        db.query("insert into test (first, second) values(%s, %s)", ('third value', 789))

        db.query('select * from test')
        r = db.fetchone()
        self.assertEqual(r[0], 'first value')
        self.assertEqual(r[1], 123)

        r = db.fetchall()
        self.assertEqual(r[0][0], 'second value')
        self.assertEqual(r[0][1], 456)
        self.assertEqual(r[1][0], 'third value')
        self.assertEqual(r[1][1], 789)

if __name__ == '__main__':
    unittest.main()
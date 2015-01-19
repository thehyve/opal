#!/usr/bin/env python2

from __future__ import absolute_import, division, print_function

import pytest

from pprint import pprint

import pycurl
import opal
import opal_tools_lib
from opal_tools_lib import *


opal_tools_lib.login_info.data = dict(
    server = 'http://localhost:8080',
    user = 'administrator',
    password = 'password',
)
project = '_test'
database = 'opal_data'

opal_upload_cmd = 'opal_upload'


ds = '/datasource/'+project+'/'





def setup_module():
    # See if project exists and we have access
    # doesn't currently test write access
    #raise Exception("error!")
    #assert rest_json(ds+'tables') == [], "Project {0} is not empty".format(project)
    pass


def teardown_module():
    # remove all left over tables
    tables = [t['name'] for t in rest_json(ds+'tables')]
    if tables:
        print("Warning: project {0} not empty after running tests".format(project))
        rest_json(ds+'tables', params=('table='+t for t in tables), method='DELETE')

def test_test():
    print("testing!")


def test_create_delete_table():
    rest_json(ds+'tables', method='POST', content={'name': 'testtable', 'entityType': 'Participant'})
    rest_json(ds+'tables?table=testtable', method='DELETE')





datafiles = {
'1upload._test.tbl.update.csv':
"""id,name,age,blob
1,Bess,9,blob1.txt
2,Modesta,46,blob2.txt
3,Houston,13,blob3.txt
4,Ty,13,blob1.txt
5,Raelene,52,blob2.txt
6,Argentina,69,blob3.txt
7,Ricki,58,blob1.txt
8,Amos,2,blob2.txt
9,Jannie,94,blob3.txt
10,Latoria,40,blob1.txt
11,Waldo,9,blob2.txt
12,Sophie,62,blob3.txt
13,Wilber,72,blob1.txt
14,Alexa,15,blob2.txt
15,Susanna,30,blob3.txt
16,Clay,44,blob1.txt
17,Leota,27,blob2.txt
18,Donetta,57,blob3.txt
19,Vanesa,42,blob1.txt
20,Yolonda,36,blob2.txt
21,Jarrett,78,blob3.txt
22,Ruthann,101,blob1.txt
23,Bailey,53,blob2.txt
24,Classie,53,blob3.txt
25,Clarisa,84,blob1.txt
26,Delbert,95,blob2.txt
27,Lashonda,58,blob3.txt
28,Robin,9,blob1.txt
29,Nigel,16,blob2.txt
30,September,57,blob3.txt
31,Monet,72,blob1.txt
32,Todd,49,blob2.txt
33,Loida,25,blob3.txt
34,Vivien,85,blob1.txt
35,Leonia,34,blob2.txt
36,Tiara,63,blob3.txt
37,Barrie,6,blob1.txt
38,Margie,79,blob2.txt
39,Angle,74,blob3.txt
40,Windy,33,blob1.txt
41,Myrna,88,blob2.txt
42,Christoper,15,blob3.txt
43,Susannah,23,blob1.txt
44,Hugh,104,blob2.txt
45,Hsiu,100,blob3.txt
46,Wanda,62,blob1.txt
47,Carolina,99,blob2.txt
48,Anne,37,blob3.txt
49,Dorian,58,blob1.txt
50,Luella,7,blob2.txt
51,Salena,103,blob3.txt
52,Delorse,68,blob1.txt
53,Florentino,60,blob2.txt
54,Khalilah,42,blob3.txt
55,Dudley,23,blob1.txt
56,Jacob,83,blob2.txt
57,Eloise,52,blob3.txt
58,Lady,62,blob1.txt
59,Eva,33,blob2.txt
60,Candelaria,93,blob3.txt
61,Whitney,15,blob1.txt
62,Santos,66,blob2.txt
63,Suanne,41,blob3.txt
64,Avelina,38,blob1.txt
65,Darrin,21,blob2.txt
66,Domonique,103,blob3.txt
67,Fran,4,blob1.txt
68,Mario,34,blob2.txt
69,Cinthia,87,blob3.txt
70,Chantay,76,blob1.txt
71,Lance,13,blob2.txt
72,Arnoldo,71,blob3.txt
73,Lesli,71,blob1.txt
74,Ariana,60,blob2.txt
75,Porter,92,blob3.txt
76,Fatima,104,blob1.txt
77,Charlie,40,blob2.txt
78,Sean,87,blob3.txt
79,Bonnie,78,blob1.txt
80,My,8,blob2.txt
81,Johnie,99,blob3.txt
82,Teresia,80,blob1.txt
83,Mitchel,58,blob2.txt
84,Caryl,27,blob3.txt
85,Jayson,8,blob1.txt
86,Gigi,63,blob2.txt
87,Rhoda,100,blob3.txt
88,Kent,81,blob1.txt
89,Saundra,12,blob2.txt
90,Rogelio,93,blob3.txt
91,Sachiko,63,blob1.txt
92,Candis,97,blob2.txt
93,Esta,95,blob3.txt
94,William,53,blob1.txt
95,Jamika,78,blob2.txt
96,Arnita,10,blob3.txt
97,Florence,34,blob1.txt
98,Jammie,25,blob2.txt
99,Joy,46,blob3.txt
100,Darron,8,blob1.txt
""",

'2delete1to10._test.tbl.delete.csv':
"""id
1
2
3
4
5
6
7
8
9
10
""",

'3update._test.tbl.update.csv':
"""id,name,age,blob
6,Argentina,12,blob4.txt
7,Ricki,11,blob4.txt
8,Amos,4,blob4.txt
9,Jannie,15,blob4.txt
10,Latoria,10,blob4.txt
11,Waldo,9,blob4.txt
12,Sophie,12,blob4.txt
13,Wilber,8,blob4.txt
14,Alexa,9,blob4.txt
15,Susanna,14,blob4.txt
""",

'41replace._test.tbl.replace.csv':
"""id,name,age,blob
1,Piet,20,blob3.txt
2,Kees,21,blob2.txt
3,Jan,22,blob1.txt
4,Wim,23,blob3.txt
5,Anna,24,blob2.txt
6,Tom,25,blob1.txt
7,Maria,26,blob3.txt
8,Pim,27,blob2.txt
9,Emma,28,blob1.txt
10,Lotte,29,blob3.txt
"""
}


if __name__ == '__main__':
    pytest.main([__file__] + sys.argv)


#!/usr/bin/env python3

import sys
import elasticsearch, elasticsearch.helpers
import random
from collections import namedtuple
from pprint import pprint
import time, math, timeit
import logging
import itertools
logging.basicConfig()

host = 'localhost:9200'
index = 'testindex'
random.seed("myrandomseed")

es = elasticsearch.Elasticsearch(host)


def reset():
    if es.indices.exists(index=index):
        es.indices.delete(index=index)

    es.indices.create(index=index)

def actions(docs, random=random):
    randplus = random.randint(1, 1000000)
    randmult = random.randint(1, 10)

    for d in docs:
        d['patient_id'] += randplus
        d['patient_id'] *= randmult
        yield dict(
            _id = d['id'],
            _source = d,
            )

def rand(size):
    for i in range(1, size+1):
        yield dict(id=i, patient_id=random.choice((1,2,3,4)), data=random.choice(('foo', 'bar', 'baz', 'quux')))

def largesmall(size):
    "Five patients, three with 1 sample each, two with many samples"
    singles = random.sample(range(1, size+1), 3)
    npid = 1
    other_ids = itertools.cycle(range(4, 1001))

    for i in range(1, size+1):
        if i in singles:
            patient_id = npid
            npid += 1
        else:
            patient_id = next(other_ids)
        yield dict(id=i, patient_id=patient_id, data=random.choice(('foo', 'bar', 'baz', 'quux')))

def alldifferent(size):
    ids = itertools.count(1)
    for i in range(1, size+1):
        yield dict(id=i, patient_id=next(ids))

def doubles(size):
    ids = itertools.count(1)
    counter = iter(range(1, size+1))
    while True:
        i = next(counter)
        nextid = next(ids)
        yield dict(id=i, patient_id=nextid)
        i = next(counter)
        yield dict(id=i, patient_id=nextid)


def stats(results, expected=None, title=None):
    if title:
        print(title)
    print("cardinalities found: {}".format(results))
    avg = sum(results)/len(results)
    if expected is None:
        expected = avg
    print("min: {min} ({minp:.2%}), max: {max} ({maxp:.2%}), avg: {avg} ({avgp:+.2%})".format(
        min=min(results), max=max(results), avg=avg,
        minp=(expected-min(results))/expected, maxp=(max(results)-expected)/expected,
        avgp=(avg-expected)/expected))
    

def run_dataset(dataset, threshold, expected=None, size=10000, runs=5, random=random):
    #print("running query {q}, size {s}, threshold {t}".format(q=dataset.__name__, s=size, t=threshold))

    # query = {
    #     "aggregations": {
    #         "my_card": {
    #             "cardinality": {
    #                 "field": "patient_id",
    #                 "precision_threshold": threshold
    #             }
    #         }
    #     }
    # }

    query = dict(aggregations=dict(my_card=dict(cardinality=dict(
        field='patient_id',
        precision_threshold=threshold,
    ))))

    cards = []

    try:
        for i in range(runs):
            print('.', end='')
            #print("run "+str(i+1), end='')
            sys.stdout.flush()


            reset()
            elasticsearch.helpers.bulk(es, actions(dataset(size), random=random),
                                      refresh=True, doc_type='testitem', index=index)
            res = es.search(index=index, doc_type='testitem', body=query, search_type='count')
            card = res['aggregations']['my_card']['value']
            cards.append(card)
            #print(" found "+str(card))
    finally:
        pass

    return cards


by_data = {
    "aggregations": {
        "my_agg": {
            "terms": {
                "field": "data",
            },
            "aggregations": {
                "my_card": {
                    "cardinality": {
                        "field": "patient_id",
                        "precision_threshold": 500
                    }
                }
            }
        }
    }
}

set_card = {
    "aggregations": {
        "my_card": {
            "cardinality": {
                "field": "patient_id",
                "precision_threshold": 100
            }
        }
    }
}

# size = 210000
# run_dataset(alldifferent, 500, size=size, expected=size)

# for size in [20000, 20001, 20010, 20100, 21000, 30000]:
#     seed = random.randint(0, 2**64-1)
#     rand = random.Random()
#     rand.seed(seed)
#     run_dataset(alldifferent, 100, size=size, expected=size, random=rand)
#     rand.seed(seed)
#     run_dataset(doubles, 100, size=size*2, expected=size, random=rand)


"""
thresholds: 100, 500, 1000, 5000, 10000, 20000, 40000
cardinality sizes: 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000
runs: 10
"""

setting = namedtuple('setting', ('threshold', 'cardinality'))

def run():
    thresholds = [100, 500, 1000, 5000, 10000, 20000, 40000]
    cardinalities = [1000000] #[1, 10, 100, 1000, 10000, 100000, 1000000, 10000000]
    idx = iter(itertools.count(1))
    combinations = [(next(idx), thr, card) for thr in thresholds for card in cardinalities]
    results = {}

    try:
        for i, thr, card in combinations:
            print("simulation {}/{}".format(i, len(combinations)), end='')
            results[setting(thr, card)] = res = run_dataset(alldifferent, thr, size=card, expected=card, runs=10)
            stats(res, expected=card, title="threshold {}, cardinality {}:".format(thr, card))

    finally:
        pprint(results)

        for (thr, card), res in sorted(results.items()):
            print('')
            stats(res, expected=card, title="threshold {}, cardinality {}:".format(thr, card))

    return results

#run()


def run2():
    size = 100000
    threshold = 40000

    def setupfn():
        return
        reset()
        elasticsearch.helpers.bulk(es, actions(alldifferent(size), random=random),
                                   refresh=True, doc_type='testitem', index=index)

    query = dict(
        aggregations=dict(my_card=dict(cardinality=dict(
            field='patient_id',
            precision_threshold=threshold,
        ))),


    )

    cards = []
    def timefn():
        nonlocal cards
        cards.append(es.search(index=index, doc_type='testitem', body=query, search_type='count'))

    times = [t/1*1000 for t in timeit.repeat(timefn, setup=setupfn, repeat=100, number=1)]
    print("Querying {size} items with threshold {t} took {n} ms".format(size=size, t=threshold, n=min(times)))
    print("times: "+str(times))

    #stats([c['aggregations']['my_card']['value'] for c in cards], expected=size)

run2()
This is a WIP.

# Basic listing of Elements

You can list your collections by entering

```show collections```

in order to list all elements in a collection type

```db.collectionname.find()```

for example, to list all elements in livetraindata, use `db.livetraindata.find()`.

# Filtering data
## Expressions

In MongoDB, different things like operators, comparisons are written as expressions.
each expression is surrounded by curly brackets:

`{ expression }`

these expressions can then be put into functions, for example if we want to filter for a firstTrainID "4050", we would use the following script:

`db.getCollection('livetraindata').find({firstTrainNumber: 49050})`

There are many different operators like _$and_, _$or_, _$match_ (which is the SQL-where) 

If we want to query for two possible trainIds, we would write it like this:

```
db.getCollection('livetraindata').find(
{$or: [
    {firstTrainNumber: 49050},
    {firstTrainNumber: 444310}
    ]}
)
```

Operators enclose their operands with [square brackets]. These can then hold as many operands as you like.

## complex aggregations

MongoDB also supports SQL-like queries. For this, use the `.aggregate([])` function on the collection. An aggregations is done in a pipeline, each operand passing its documents to the next part. The operands can be defined inside the square brackets. Consider the following example: 


```
db.getCollection('livetraindata').aggregate([
    {$match: 
        {firstTrainNumber: 49050}        
    },
    {$lookup : 
        {
        localField: "locationId",
        from: "locationdata",
        foreignField: "id",
        as: "locationdata"
        }
    },

    {$match : 
            {$or: [
                {"locationdata.country":"DE"},
                {"locationdata.country":"AT"},
                {"locationdata.country":"CH"}
            ]}
    }
]);
```
This script would first filter each entry for its firstTrainNumber ($match), join it with the locations collection on the locationId (&lookup), and lastly filter once again to only include specific countries.
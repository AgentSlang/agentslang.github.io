# Basic demos

## Basic working test

To ensure AgentSlang is correctly installed, you can use the basic_test.xml test configuration.

```
# Inside bin directory of AgentSlang installation folder
cd ${AGENTSLANGINSTALLDIR}/bin

# In GNU/Linux
./AgentSlang -config ../config/test_configurations/basic_test.xml -profile profile1

# In Windows
AgentSlang -config ../config/test_configurations/basic_test.xml -profile profile1
```

If everything's working fine, you should have this output in your terminal
```
(INFORM)[org.ib.bricks.Test2] {id=1, language=none, data='Hello-t2:1'}
(INFORM)[org.ib.bricks.Test2] {id=0, language=none, data='Hello-t1:0'}
(INFORM)[org.ib.bricks.Test2] {id=1, language=none, data='Hello-t1:1'}
(INFORM)[org.ib.bricks.Test2] {id=2, language=none, data='Hello-t2:2'}
(INFORM)[org.ib.bricks.Test2] {id=2, language=none, data='Hello-t1:2'}
```
nodes = [] #(x, y)
edges = [] #([x1, x2], [y1, y2])
startNode = False
startEdge = False
linked = []
with open('input', 'r') as f:
    for line in f:
        line = line.strip()
        if not line:
            continue
        if 'xloc' in line:
            startNode = True
        elif 'nodeid1' in line:
            linked = [False for i in range(len(nodes))]
            startEdge = True
        elif startEdge:
            temp = map(int, line.split(','))
            node1 = nodes[temp[0]]
            node2 = nodes[temp[1]]
            linked[temp[0]] = True
            linked[temp[1]] = True
            edges.append(([node1[0], node2[0]], [node1[1], node2[1]]))
        elif startNode:
            temp = map(int, line.split(','))
            nodes.append((temp[1], temp[2]))
        
for i in range(len(linked)):
    if linked[i] == False:
        print i, nodes[i]
import matplotlib.pyplot as plot
for node in nodes:
    plot.plot(node[0], node[1], 'ro')
for edge in edges:
    plot.plot(edge[0], edge[1], 'b-');
plot.axis([-1, 21, -1, 11])
plot.show()

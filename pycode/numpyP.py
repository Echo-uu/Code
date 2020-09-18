#2020.9.18 Fri
#基础感知：线性拟合数据

import numpy as np
import matplotlib.pyplot as plt

def linearT():
    #original data
    X = [1, 2, 3, 4, 5, 6]
    Y = [2.6, 3.4, 4.7, 5.5, 6.47, 7.8]

    #一次多项式拟合(linear)
    z1 = np.polyfit(X, Y, 1)
    p1 = np.poly1d(z1)
    print(z1)
    print(p1)

    #作图
    x = np.arange(1, 7)
    y = z1[0] * x + z1[1]
    plt.figure()
    plt.scatter(X, Y)
    plt.plot(x, y)
    plt.show()
    return

#linearT()

#cost time
def costTime():
    myArr = np.arange(1000000)
    myList = list(range(1000000))
    print(myArr)
    #%time for _ in range(10): myArr2 = myArr * 2
    return

def ndarrayTest():
    data = np.random.randn(2, 3)
    print(data, data + data, data * 10)
    print('data shape', data.shape, '\n', 'data dtype', data.dtype)
    print('data ndim', data.ndim)
    return

ndarrayTest()

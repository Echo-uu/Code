#2020.9.18 Fri
#基础感知：线性拟合数据

import numpy as np
import matplotlib as mpl
import matplotlib.pyplot as plt

#%matplotlib inline

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
    data = np.random.randn(6, 3)
    print(data, data + data, data * 10)
    print('data shape', data.shape, '\n', 'data dtype', data.dtype)
    print('data ndim', data.ndim)
    #
    numericStrings = np.array(['1.456', '2.545', '454.55'], dtype = np.string_)
    print(numericStrings)
    print(numericStrings.astype(float))
    #slice修改原数组
    arr = np.arange(10)
    print(arr)
    arrSlice = arr[3:8]
    arrSlice -= 1
    print(arr)
    #
    arr1 = np.arange(32).reshape(8,4)
    print(arr1)
    print(arr1[[1, 5, 7, 2]][:,[0, 3, 1, 2]])
    #矩阵转置，内积
    print(data.T)
    print(np.dot(data.T,data))
    return

#ndarrayTest()

#2020.9.19 Sat
#通用函数(ufunc)：快速的元素级数组函数
#是一种对ndarray中的数据执行元素级运算的函数。
# 你可以将其看做简单函数（接受一个或多个标量值，
# 并产生一个或多个标量值）的矢量化包装器。
def ufuncTest():
    arr0 = np.arange(20)
    print('\n\n', np.sqrt(arr0))
    print('\n\n', np.exp(arr0))
    
    arr1 = np.random.rand(20)
    print('\n\n', np.maximum(arr0, arr1))
    
    #modf()返回整数和小数
    print('\n\n', np.modf(arr1))

    #np.meshgrid函数接受两个一维数组
    #并产生两个二维矩阵（对应于两个数组中所有的(x,y)对）
    pointX = np.arange(-5,5,0.01)
    pointY = np.arange(-5,5,0.01)
    x, y = np.meshgrid(pointX, pointY)
    print('\n\n', x, '\n\n', y)
    print('\n\n', np.sqrt(x ** 2 + y ** 2))
    plt.imshow(np.sqrt(x ** 2 + y ** 2), cmap = mpl.cm.gray); plt.show(); #plt.colorbar()
    plt.title("Image plot of $\sqrt{x^2 + y^2}$ for a grid of values")
    
    #np.where && x if condition else y
    cond = np.random.choice([True, False], (20))

    #result = [(X if c else Y)for X, Y, c in zip(arr0, arr1, cond)]
    #print('\n\n', result)
    print('\n\n', [(X if c else Y)for X, Y, c in zip(arr0, arr1, cond)])
    print('\n\n', np.where(cond, arr0, arr1))

    #np.random.randn()从标准正态分布中返回一个或多个样本值
    Nd = np.random.randn(5, 4) 
    print('\n', 'Normal Distribution Data\n', Nd)
    #axis = 0 column value; axis = 1 row value
    print('\n', Nd.mean(), np.mean(Nd))#average value
    print('\n', Nd.sum())
    print('\ncumsum\n', Nd.cumsum())
    print('\ncumprod\n', Nd.cumprod())
    print('\nstd\n', Nd.std())
    print('\nvar\n', Nd.var())
    print('\nany(), or()\n', cond.any(), cond.all())
    
    return

ufuncTest()
### Log

#### 3.10 改了代码 （手动拉动调节CORRECTION_GAIN）



### 3.16 （回忆）

较暗 背光 脸部两侧有白光 手机屏幕光打在脸上

相机设置应该是也是手动设置了rgb gain

##### ①Sinan 17：32

有broken file broken signal

有两张有1hz noise 明显

其他还好 但是有其他噪声

##### ②Yuki 18：00

有broken signal broken signal

一张1hz noise 明显

有两张predict 的peak 错误 其他图有些许噪声



#### 3.17 （回忆）

背光

Dirk_21_male (overwritten on 3.23 running 没有被overwritten)

环境较暗(16:29) 画面暗

设置的RGB GAIN应该较小

结果：
三张效果不错 有三张有1hz noise（一张是incandescent 频闪）



#### 3.18（回忆） 

背光

Dirk2 & Dirk3 12：56 设置了快门速度3500000 有很明显的1hz noise 每张都有



#### 3.19（回忆）

##### ①Dirk4 11：21 

面对阳光

设置回了2000000的快门速度

手动设置COLORCORRECTION_GAIN（设置和以前一样）

结果大致较好 有些许1hz

broken signal



##### ②Dirk5 12：46

面对阳光

有设置成了3500000的快门速度（对照试验）

手动设置COLORCORRECTION_GAIN（设置和以前一样）

测了三个

效果很差  1hz noise明显



#### 3.22 （地点为4-511）

均为面对阳光

##### ①Dirk6_21_male

上午10.15，晴

手机屏幕亮

头处于画面中间，填满

黑衣服 背景融为一体



![image-20210322115824557](C:\Users\40957\AppData\Roaming\Typora\typora-user-images\image-20210322115824557.png)

结果非常明显的1hz noise



##### ② Dirk7_21_male

上午10.30，晴

设置同上

手机屏幕暗（应该是无关项）

头处于画面中间，填满

非常明显的1hz noise

黑衣服 背景融为一体（应该是无关项）



##### ③ Dirk8

上午12：40，晴

设置同上

录了2个

明显的1hz

因为当时心率接近60，所以mae较小



##### ④ Dirk9

13：42，晴

设置同上

录了5个

较明显的1hz（led stationary 没有 其他都有）



##### ⑤ DIRK10

14：10， 晴

浅色衣服（为了验证和衣服颜色的关系） 发现好像没有关系

设置同上

录了4个

明显的1hz



##### ⑥ DIRK11

设置为：

![image-20210322151359804](C:\Users\40957\AppData\Roaming\Typora\typora-user-images\image-20210322151359804.png)

效果较好（有1hz突起 但是大部分不明显 有一个比较明显的）



##### ⑦ DIRK12

设置为：
![image-20210322151330068](C:\Users\40957\AppData\Roaming\Typora\typora-user-images\image-20210322151330068.png)

1hz非常明显 结果很烂







（怀疑是只设置CORRECTION_GAIN，放大了噪声，视频里噪点很明显。应该通过调节ISO和快门速度来来调节）



#### 3.23

##### ①Dirk13

没有设置任何 应该是挂了自动挡（？）

早上8：30  晴

4-511

![image-20210323085617712](C:\Users\40957\AppData\Roaming\Typora\typora-user-images\image-20210323085617712.png)

设置成了30s

结果：



##### ②

DIRK14 重复 9：10 晴

亮度？ LED



LED 和 incandescent 没法完全遮住



##### ③ 

Dirk15 9：30 晴（和①②的对照试验）

![image-20210323093632334](C:\Users\40957\AppData\Roaming\Typora\typora-user-images\image-20210323093632334.png)

RGBValue 拉到了20左右 噪点很大

结果：1hz noise明显


1.netty的 NioServerSocketChannel 是对jdk  ServerSocketChannel 的封装

2.io.netty.channel是对socket的一个连接 ,或者一个组件处理IO操作 如读，写，连接，绑定

3.channelPipline可以处理和当前channel关联的全部的io事件和channel所关联的请求，
  channelPipline可以将若干事件处理器组合起来


  All I/O operations are asynchronous.
  All I/O operations in Netty are asynchronous.
  It means any I/O calls will return immediately with no guarantee that the requested I/O operation has been completed
  at the end of the call.
  Instead, you will be returned with a ChannelFuture instance
  which will notify you when the requested I/O operation has succeeded, failed, or canceled.


4.channelPipline何时创建:
创建channel的时候 追溯到其父类AbstractChannel的构造方法中创建了channelPipLine

5.ChannelPipline和channel的关系：
    ChannelPipline中包含了channel 创建时传入
    channel中也包含了ChannelPipLine


6.ChannelPipline是个啥
ChannelHandler的列表，用于处理或拦截Channel的入站事件和出站操作。
ChannelPipeline实现了Intercepting Filter模式的高级形式，
以使用户可以完全控制事件的处理方式以及管道中的ChannelHandlers如何交互。
每个Channel都有其自己的pipline，并且在创建新channel时会自动创建它。
channelPipline中维护了一个双向链表



472/5000 channelInbound
入站事件由入站处理程序按自下而上的方向进行处理，如图中左侧所示。
 入站处理程序通常处理图底部的I / O线程生成的入站数据。
 通常通过实际的输入操作（例如SocketChannel.read（ByteBuffer））从远程对等方读取入站数据。
 如果入站事件超出了顶部入站处理程序的范围，则将其静默丢弃，或者在需要引起注意时记录下来。


439/5000 channelOutBound
出站事件由出站处理程序按自上而下的方向进行处理，如图中右侧所示。 出站处理程序通常会生成或转换出站流量，例如写请求。
 如果出站事件超出了底部出站处理程序，则由与通道关联的I / O线程处理。
 I / O线程通常执行实际的输出操作，例如SocketChannel.write（ByteBuffer）。


 处理程序必须调用ChannelHandlerContext中的事件传播方法以将事件转发到其下一个处理程序。
   public class MyInboundHandler extends ChannelInboundHandlerAdapter {
         @Override
        public void channelActive(ChannelHandlerContext ctx) {
            System.out.println("Connected!");
            ctx.fireChannelActive();//重点是指这句
        }
    }


---------------------------------------------------------------------------------------------------------------------
ChannelHandlerContext

233/5000
使ChannelHandler与其ChannelPipeline和其他处理程序进行交互。
 处理程序除其他外，可以通知ChannelPipeline中的下一个ChannelHandler以及动态修改其所属的ChannelPipeline。


您可以通过调用此处提供的各种方法之一来通知同一ChannelPipeline中最接近的处理程序。 请参考ChannelPipeline以了解事件的流向。
修改管道
您可以通过调用pipeline（）来获取处理程序所属的ChannelPipeline。 一个非平凡的应用程序可以在运行时动态地在管道中插入，删除或替换处理程序。
检索以备后用

netty区分入站处理器和出站处理器 通过 判断 这个处理器实现了哪个接口进行区分
入站处理器 实现了ChannelInboundHandler
出站处理器 实现了ChannelOutboundHandler

//绑定了当前channel的pipline 和 channelHandler
DefaultChannelHandlerContext(
            DefaultChannelPipeline pipeline, EventExecutor executor, String name, ChannelHandler handler) {
        super(pipeline, executor, name, handler.getClass());
        this.handler = handler;
    }

DefaultChannelPipline中维护了DefaultChannelHandlerContext的链表
 private void addLast0(AbstractChannelHandlerContext newCtx) {
        AbstractChannelHandlerContext prev = tail.prev;
        newCtx.prev = prev;
        newCtx.next = tail;
        prev.next = newCtx;
        tail.prev = newCtx;
    }

-----------------------------------------------------------------------------------------------------------------------------------
io.netty.channel.ChannelInitializer<C extends Channel>
protected abstract void initChannel(C ch) throws Exception
This method will be called once the Channel was registered. After the method returns this instance will be removed from the ChannelPipeline of the Channel.
Inisializer会在先添加最后会被移除掉在  ChannelInitializer-handlerAdded方法
-----------------------------------------------------------------------------------------------------------------------------------
Netty channel注册
 ChannelFuture regFuture = config().group().register(channel);
--
 config()
    Returns the {@link AbstractBootstrapConfig} object that can be used to obtain the current config  of the bootstrap.
    返回ServerBootStrapConfig对象
--
 group()
    Returns the configured {@link EventLoopGroup} or {@code null} if non is configured yet.
    返回EventLoopGroup（事件循环组） NioEventLoopGroup
--
注册
 register(channel)
    会进入MultiThreadEventLoopGroup这个类(NioEventLoopGroup)的父类执行register方法
 MultiThreadEventLoopGroup:   Abstract base class for EventLoopGroup implementations that handles their tasks with multiple threads at the same time

SingleThreadEventLoop:Abstract base class for EventLoops that execute all its submitted tasks in a single thread.
通过 chooser 获取到 EventLoop（SingleThreadEventLoop）  去注册 channel

abstractChannel  register方法（eventLoop(SIngleThreadEventLoop)）SIngleThreadEventLoop(其中维护了一个线程)
if (eventLoop.inEventLoop()) { //如果当前调用此方法的线程就是SingleThreadEventLoop关联的线程 执行注册
                register0(promise);
            } else {//否则交给singleThreadEventLoop的那个线程异步执行
                try {
                    eventLoop.execute(new Runnable() {
                        @Override
                        public void run() {
                            register0(promise);
                        }
                    });
                } catch (Throwable t) {
                    logger.warn(
                            "Force-closing a channel whose registration task was not accepted by an event loop: {}",
                            AbstractChannel.this, t);
                    closeForcibly();
                    closeFuture.setClosed();
                    safeSetFailure(promise, t);
                }
            }

为什么要进行当前线程判断呢：
1个EventLoopGroup中包含一个或者多个EventLoop
1个EventLoop在他的整个生命周期中只会于一个thread进行绑定而这个thread就是io线程,间接继承了SingleThreadEventExecutor
所有由eventLoop处理的io事件都将由他关联的thread上处理
1个channel在他的整个生命周期中，只会注册在一个eventLoop上
1个eventLoop在运行过程中会被分配给1个或多个channel
netty中channel的实现是线程安全的，我们可以存储一个channel的引用，且需要向远程端点发送数据时通过这个引用调用channel相关的方法，
消息会按顺序发送出去(channel对应的eventLoop对应的singleThreadEventLoop的父类singleThreadExecutor中维护了一个队列)
最后执行最底层的注册逻辑  将channel注册到selector上
--------------------------------------------------------------------------------------------------------------------------
java.util.concurrent(future)
ip.netty(future)   addListener
可以添加监听器
Listens to the result of a Future.
The result of the asynchronous operation is notified once this listener is added by calling Future.addListener(GenericFutureListener).
void operationComplete(F future)
throws Exception
Invoked when the operation associated with the Future has been completed.

future负责添加监听器  在future的子类中具体通知添加好的监听器（观察者模式）

ChannelFutureListener:
Listens to the result of a ChannelFuture.
The result of the asynchronous Channel I/O operation is notified once this listener is added by calling ChannelFuture.addListener(GenericFutureListener).
Return the control to the caller quickly
operationComplete(Future) is directly called by an I/O thread.
 Therefore, performing a time consuming task or a blocking operation in the handler method can cause an unexpected pause during I/O.
 If you need to perform a blocking operation on I/O completion,
try to execute the operation in a different thread using a thread pool.
--------------------------------------------------------------------------------------------------
io.netty.channel public interface ChannelPromise（Special {@link ChannelFuture} which is writable.）
extends ChannelFuture, Promise<Void>
Special ChannelFuture which is writable.

Promise:
 /**
     * Marks this future as a success and notifies all listeners. 标记这个future执行成功后会通知全部监听器
     */
    Promise<V> setSuccess(V result);


 jdk future通过get 获取执行结果
 而netty的 future通过观察者模式的应用当future执行完成后 已回调的方式获取执行结果
 channelfuturelistener的operationComplete是由io线程调用的 所以其中不能有耗时操作
--------------------------------------------------------------------------------------------------
ChannelInboundHandler
which adds callbacks for state changes. This allows the user to hook in to state changes easily.
//channel注册完成后会进行回调,里面有好多回调方法
/**
     * The {@link Channel} of the {@link ChannelHandlerContext} was registered with its {@link EventLoop}
     */
    void channelRegistered(ChannelHandlerContext ctx) throws Exception;

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} was unregistered from its {@link EventLoop}
     */
    void channelUnregistered(ChannelHandlerContext ctx) throws Exception;

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} is now active
     */
    void channelActive(ChannelHandlerContext ctx) throws Exception;

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} was registered is now inactive and reached its
     * end of lifetime.
     */
    void channelInactive(ChannelHandlerContext ctx) throws Exception;

    /**
     * Invoked when the current {@link Channel} has read a message from the peer.
     */
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;
-----------------------------------------------------------------------------------------------------------------------
 复习：
 NIO byteBuffer
1.从inputStream获取channel对象
2.创建buffer
3.将数据从channel对象中读取到buffer中

ByteBuffer  :mark,position,limit ,capicity
position:即将要读取的元素位置<limit
limit:初始时等于cap flip时limit也是最后一个元素的位置，flip()时limit不变,position回到最初的位置
flip()方法作用： 1.将limit值设置为当前的position
                2.将position设置为0
clear()方法作用：
                1.将limit值设置为capacity
                2.将position设置为0
compact方法的作用：
                1.将所有未读的数句放到起始位置
                2.positon放到最后一个未读元素后
                3.limit设置为capacity
                4.buffer数据准备好但是不会覆盖未读数据

DirectByteBuffer 分配到堆上但是持有了一个内存地址指针 指针指向了   堆外本地内存，通过c语言malloc分配到堆外 且是用户态

FileChannel的read函数，write函数中，如果传入的参数是HeapBuffer类型，则会临时申请1块儿DirectBuffer进行1次数据拷贝，而不是直接进行数据传输，这是为什么：
代码流程：1判断传入的buffer如果是DirectBuffer 则这接堆外内存
         如果传入的入参是HeapBuffer堆内内存，然后分配出一个直接内存 ，然后HeapBuffer拷贝到直接内存， 然后处理write写出
    之所以这么做是因为 hotsport的垃圾回收会移动对象的 compacting GC,如果把java字节数组byte[]引用传给native代码，让native代码直接访问数组的内容的话，
    需要保证native代码在访问时byte数组这个字节对象不能移动，所以会影响jvm垃圾回收，让整个堆不会被回收，则会有问题，所以将堆数据拷贝到（不发生GC） 直接缓冲区， 这样堆中的数据垃圾回收也没关系。
------------------------------------------------------------------------------------------------------------------------------------------------------------------------
NETTY  io.netty.buffer.ByteBuf

ByteBuf 内部有两个指针 readerIndex writerIndex    wirterIndex指向当前写的位置，readerIndex指向当前读的位置 ，这样在读写过程中 ByteBuf内部会被分为3个区域

        +-------------------+------------------+------------------+
        | discardable bytes |  readable bytes  |  writable bytes  |
        |                   |     (CONTENT)    |                  |
        +-------------------+------------------+------------------+
        |                   |                  |                  |
        0      <=      readerIndex   <=   writerIndex    <=    capacity


 discardable bytes  可丢弃的 因为这部分内容已经读取完毕
 readable bytes 可读内容
 writeable bytes 继续可写的区域
 有两个指针好处就是进行读取切换的时候不需要像flip()方法做切换
 buffer.getByte(i) 不会改变读索引
 buffer.readByte() 会改变读索引
 readerIndex()会改变索引
 writerIndex()会改变索引

 netty ByteBuf所提供的3种缓冲区类型
 1. heapBuffer
 2.Direct Buffer
 3.composite buffer (复合缓冲区) 用的多

CompositeByteBuf(将多个缓冲区合并成单个缓冲区)
A virtual buffer which shows multiple buffers as a single merged buffer.
通过这个方法创建   ByteBufAllocator.compositeBuffer()
 ByteBuf heapBuf = Unpooled.buffer(10);        //UnpooledUnsafeHeapByteBuf
 ByteBuf directBuf = Unpooled.directBuffer(8); //UnpooledUnsafeNoCleanerDirectByteBuf

---------------------------------------------------------------------------------------------------------------------------------------------
netty ByteBuf

Heap Buffer（堆缓冲区）
这是最常用的类型，ByteBuf将数据存到JVM堆空间中，并将实际的数据存放到 byte array中来实现
优点：由于数据是JVM堆中，可以快速创建和快速释放，提供访问内部字节数组的方法。
缺点：每次读写数据时都需要先将数据复制到直接缓冲区中，再进行网络传输。

Direct BUffer（直接缓冲区）
在堆之外直接分配内存空间，直接缓冲区不会占用堆的容量空间，由操作系统在本地内存进行的数据分配
优点： 使用socket进行数据传递时性能好，数据在操作系统的本地内存中，不需拷贝步骤
缺点： 因为Direct Buffer是直接在操作系统内存中的，内存空间分配与释放比堆空间更加复杂，且速度慢一些。

NEtty通过提供内存池来解决这个问题。直接缓冲区不支持通过字节数组的方式访问其中的数据

重点：对于后端的业务消息编解码来说推荐使用HeapByteBuf,对于IO通信线程在读写缓冲区时，推荐使用DirectByteBuf

Composite Buffer(复合缓冲区)
里面可以存储多个多种类型的ByteBuf
-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
jdk ByteBuffer 比对 netty ByteBuf
1.Netty的ByteBuf采用读写索引分离的策略 readerIndex ,writerIndex  一个初始化的ByteBuf  readerIndex=writerIndex=0 最后随着读写进行ByteBuf会被分成3个区域，DisCard，readable ,writeable
2.读索引写索引是同一个位置时继续读取会报错 IndexOutOfBoundsException
3.对于ByteBuf的任何读写操作 都会分别维护读索引和写索引
--
jdk ByteBuffer 的缺点
1.final byte[]  hb 这个是jdb ByteBuffer对象中用于存储数据的对象声明，可以看到，其字节数据被声明为final 也就是长度是固定不变的。一旦分配好就不能动态扩容与收缩。
如果ByteBuffer空间不足，我们只有一个解决方案，创建一个新的ByteBuffer对象，然后再将之前的ByteBUffer中的数据复制过去，这一切操作都得由开发者自己手动完成。
2.ByteBuffer只使用1个position来标识位置信息，进行读写切换时需要调用flip方法，使用起来很不方便

Netty ByteBuf优点
1.字节存储的数组是动态的，最大值是Integer。MAX_VALUE  这里的动态性是体现在write方法中的。write方法在执行时会判断buffer的容量。如果不足则进行自动动态扩容。
2.ByteBuf的读写索引是完全分开的，使用起来很方便。
----------------------------------------------------------------------------------------------------------------------------------
ReferenceCounted
一个引用计数的对象，需要显式释放。
当实例化一个新的ReferenceCounted时，它以1的引用计数开始。keep（）增加引用计数，而release（）减少引用计数。 如果引用计数减少到0，则将显式释放对象，并且访问该释放对象通常会导致访问冲突。
如果实现ReferenceCounted的对象是其他实现ReferenceCounted的对象的容器，则当容器的引用计数变为0时，包含的对象也将通过release（）释放。


AtomicIntegerFieldUpdater(jdk1.5) 原子整形字段更新器，提供了很多原子更新操作（cas）
1.更新器更新的必须是int类型变量，不能是其包装类型
2.更新器更新的必须是volatile类型变量，确保禁止指令重排序，且线程之间共享变量的可见性
------------------------------------------------------------------------------  ----

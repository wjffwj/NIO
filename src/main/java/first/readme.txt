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
--------------------------------------------------------------------------------------------------
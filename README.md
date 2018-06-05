## qtill工具库

qtill是一个基于java语言的工具库，集成了各种java开发中常用的功能，并针对常用组件进行了使用便利性方面的定制


### qtill-commons
- [ ] io
- [ ] lang
- [x] 二进制基本操作工具
- [x] native：unsafe工具

### qtill-serialization
- [x] JDK工具类
- [x] kyro工具类
- [x] Hessian工具类
- [x] JSON工具类
- [x] Google Protostuff工具类

### qtill-logging


### qtill-mybatis
- [x] 分页插件


### qtill-redis
- [x] JedisPoolBuilder工具: 提供默认值，并通过Builder模式更简单优雅地构建JedisPool。提供级联调用。
- [x] Jedix executor框架：Command模式，使开发者无需关心Jedis资源池的borrow和return操作，避免因为忘记close而造成资源池耗尽


### qtill-netty
- [x] NettyBootstrapBuilder, NettyServerBootstrapBuilder构造器辅助类，提供默认值，增加高可靠性的相关配置，方便构建Netty实例
- [x] NettyClient、NettyServer构造辅助类，提供默认值和基础的Handler（心跳包、重连、自动重发等），方便构造和配置
- [x] 可配置的自动重试功能，提供配置：最大重试次数、重试间隔
- [x] 可配置的心跳包功能，提供配置：心跳包发送、心跳包接受检验、心跳包丢失最大次数等
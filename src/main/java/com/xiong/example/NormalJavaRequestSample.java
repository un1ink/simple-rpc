package com.xiong.example;

/**
 * @description:
 * @author：un1ink
 * @date: 2023/5/29
 */
import com.xiong.annotation.RpcScan;
import com.xiong.hello.client.HelloController;
import com.xiong.hello.client.NettyClientMain;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@RpcScan(basePackage = {"com.xiong"})
public class NormalJavaRequestSample extends AbstractJavaSamplerClient {

    private SampleResult result;

    private HelloController helloController;

    /**
     * 初始化方法，用于初始化性能测试时的每个线程，每个线程测试前执行一次。
     */
    @Override
    public void setupTest(JavaSamplerContext context) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
        this.helloController = (HelloController) applicationContext.getBean("helloController");

        // 可以初始化 RPC Client
    }

    /**
     * 测试结束时调用，可释放资源等。
     */
    @Override
    public void teardownTest(JavaSamplerContext context) {
        System.out.println("NormalJavaRequestSample.teardownTest");
    }

    /**
     * 主要用于设置传入的参数和默认值，可在 Jmeter 界面显示。
     */
    @Override
    public Arguments getDefaultParameters() {
        Arguments arguments = new Arguments();

        return arguments;
    }

    /**
     * 性能测试运行体。
     */
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        result.sampleStart(); // Jmeter 开始计时

        try {
            helloController.test();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        result.sampleEnd(); // Jmeter 结束计时

        return result;
    }

    private boolean add(int a, int b) {
        System.out.println(a+b);
        return true;
    }
    /**
     * Jmeter 不会调用 main 方法，这里用于生成 Jar。
     * @param args
     */
    public static void main(String[] args) throws InterruptedException {
        NettyClientMain.clientTest();

    }
}
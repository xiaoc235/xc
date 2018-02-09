package com.common.spring;

import com.common.spring.utils.CommonUtils;
import com.common.utils.GsonUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jianghaoming on 17/5/10.
 */
@Aspect
@Component
public class WebLogAspect {

    private static final Logger _logger = LoggerFactory.getLogger(WebLogAspect.class);

    private static final String WRAN_LINE_SIGN = "\r\n";


    /**
     *
     第一个* 表示任意返回值类型
     第二个* 表示以任意名字开头的package. 如 com.xx.
     第三个* 表示以任意名字开头的class的类名 如TestService
     第四个* 表示 通配 *service下的任意class
     最后二个.. 表示通配 方法可以有0个或多个参数
     */
    @Pointcut("execution(public * *..*Controller.*(..))")
    public void webLog(){}



    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        StringBuffer requestStr = new StringBuffer("request ==> ");
        requestStr.append(WRAN_LINE_SIGN);
        requestStr.append("--------------------------------------------------------------------------------------------------");
        requestStr.append(WRAN_LINE_SIGN);
        requestStr.append("IP : " + CommonUtils.getIpAddr(request));
        requestStr.append(WRAN_LINE_SIGN);
        requestStr.append("URL : " + request.getRequestURL().toString());
        requestStr.append(WRAN_LINE_SIGN);
        requestStr.append("Method : " + request.getMethod());
        requestStr.append(WRAN_LINE_SIGN);
        requestStr.append("ContentType : " +request.getContentType());
        requestStr.append(WRAN_LINE_SIGN);
        requestStr.append("Class_Method : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        requestStr.append(WRAN_LINE_SIGN);
        requestStr.append("Body args : " +  GsonUtils.toJson(joinPoint.getArgs()));
        requestStr.append(WRAN_LINE_SIGN);
        requestStr.append("Url args : ");
        Enumeration<String> enu=request.getParameterNames();
        while(enu.hasMoreElements()){
            String paraName=(String)enu.nextElement();
            requestStr.append(" "+paraName+" = "+request.getParameter(paraName));
        }
        requestStr.append(WRAN_LINE_SIGN);
        Map<String,Object> header = new HashMap<>();
        Enumeration<String> requestHeader = request.getHeaderNames();
        while(requestHeader.hasMoreElements()){
            String key = requestHeader.nextElement();
            if(key.equals("host") || key.equals("user-agent") || key.equals("userid") || key.equals("userId") || key.contains("token")) {
                String value = request.getHeader(key);
                header.put(key, value);
            }
        }
        requestStr.append("Header args : " + header.toString());
        requestStr.append(WRAN_LINE_SIGN);
        requestStr.append("--------------------------------------------------------------------------------------------------");
        _logger.info(requestStr.toString());

        if(request.getDispatcherType().name().equals("ERROR") || request.getRequestURI().toLowerCase().endsWith("/error")){
            return;
        }
    }

    @AfterReturning(returning = "ret", pointcut = "webLog()")
    public void doAfterReturning(Object ret) throws Throwable {
        // 处理完请求，返回内容
        StringBuffer reponseStr = new StringBuffer("reponse ==> ");
        reponseStr.append(WRAN_LINE_SIGN);
        reponseStr.append("--------------------------------------------------------------------------------------------------");
        reponseStr.append(WRAN_LINE_SIGN);
        reponseStr.append(GsonUtils.toJson(ret));
        reponseStr.append(WRAN_LINE_SIGN);
        reponseStr.append("--------------------------------------------------------------------------------------------------");
        _logger.info(reponseStr.toString());
    }

}

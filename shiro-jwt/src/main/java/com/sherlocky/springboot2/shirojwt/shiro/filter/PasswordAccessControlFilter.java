package com.sherlocky.springboot2.shirojwt.shiro.filter;

import com.sherlocky.springboot2.shirojwt.constant.StatusCodeEnum;
import com.sherlocky.springboot2.shirojwt.domain.vo.ResponseBean;
import com.sherlocky.springboot2.shirojwt.shiro.token.PasswordAuthenticationToken;
import com.sherlocky.springboot2.shirojwt.shiro.util.DynamicKeyCacheUtils;
import com.sherlocky.springboot2.shirojwt.shiro.util.ServletRequestUtils;
import com.sherlocky.springboot2.shirojwt.util.RequestResponseUtil;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 基于 用户名密码 的认证过滤器
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class PasswordAccessControlFilter extends AccessControlFilter {
    private StringRedisTemplate redisTemplate;

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        Subject subject = getSubject(request, response);
        // 如果其已经登录，再此发送登录请求
        //  拒绝，统一交给 onAccessDenied 处理
        return null != subject && subject.isAuthenticated();
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        // 判断若为获取登录注册加密动态秘钥请求
        if (isPasswordTokenGet(request)) {
            // 动态生成秘钥，redis存储秘钥供之后秘钥验证使用，设置有效期5秒用完即丢弃
            String dynamicKey = RandomStringUtils.randomAlphanumeric(16);
            String userKey = RandomStringUtils.randomAlphanumeric(6);
            try {
                DynamicKeyCacheUtils.set(redisTemplate, ServletRequestUtils.host(request), userKey, dynamicKey);
                // 动态秘钥response返回给前端
                RequestResponseUtil.responseWrite(
                        ResponseBean.ok(StatusCodeEnum.DYNAMIC_KEY_ISSUED_SUCCESS)
                                .addData("dynamicKey", dynamicKey).addData("userKey", userKey),
                        response);

            } catch (Exception e) {
                log.error("签发动态秘钥失败", e);
                RequestResponseUtil.responseWrite(ResponseBean.ok(StatusCodeEnum.DYNAMIC_KEY_ISSUED_FAIL), response);
            }
            return false;
        }

        // 判断是否是登录请求
        if (isPasswordLoginPost(request)) {
            AuthenticationToken authenticationToken = null;
            try {
                authenticationToken = createPasswordToken(request);
            } catch (Exception e) {
                // response 告知无效请求
                RequestResponseUtil.responseWrite(ResponseBean.error(StatusCodeEnum.INVALID_REQUEST), response);
                return false;
            }

            Subject subject = getSubject(request, response);
            try {
                subject.login(authenticationToken);
                //登录认证成功,进入请求派发json web token url资源内
                return true;
            } catch (AuthenticationException e) {
                log.warn(authenticationToken.getPrincipal() + "::登录认证失败", e);
                // 返回response告诉客户端认证失败
                RequestResponseUtil.responseWrite(ResponseBean.error(StatusCodeEnum.LOGIN_FAIL), response);
                return false;
            } catch (Exception e) {
                log.error(authenticationToken.getPrincipal() + "::认证异常::" + e.getMessage(), e);
                // 返回response告诉客户端认证失败
                RequestResponseUtil.responseWrite(ResponseBean.error(StatusCodeEnum.LOGIN_FAIL), response);
                return false;
            }
        }
        // 判断是否为注册请求, 若是通过过滤链进入controller注册
        if (isAccountRegisterPost(request)) {
            return true;
        }
        // TODO 之后添加对账户的找回等
        // 暂时 response 告知无效请求
        RequestResponseUtil.responseWrite(ResponseBean.error(StatusCodeEnum.INVALID_REQUEST), response);
        return false;
    }

    private boolean isPasswordTokenGet(ServletRequest request) {
        String dynamicKey = RequestResponseUtil.getParameter(request, "dynamicKey");
        return (request instanceof HttpServletRequest)
                && "GET".equalsIgnoreCase(((HttpServletRequest) request).getMethod())
                && "get".equals(dynamicKey);
    }

    private boolean isPasswordLoginPost(ServletRequest request) {
        Map<String, String> map = RequestResponseUtil.getRequestBodyMap(request);
        String password = map.get("password");
        String timestamp = map.get("timestamp");
        String methodName = map.get("methodName");
        String account = map.get("account");
        return (request instanceof HttpServletRequest)
                && "POST".equalsIgnoreCase(((HttpServletRequest) request).getMethod())
                && null != password
                && null != timestamp
                && null != account
                && "login".equals(methodName);
    }

    private boolean isAccountRegisterPost(ServletRequest request) {
        Map<String, String> map = RequestResponseUtil.getRequestBodyMap(request);
        String uid = map.get("uid");
        String username = map.get("username");
        String methodName = map.get("methodName");
        String password = map.get("password");
        return (request instanceof HttpServletRequest)
                && "POST".equalsIgnoreCase(((HttpServletRequest) request).getMethod())
                && null != username
                && null != password
                && null != uid
                && "register".equals(methodName);
    }

    private AuthenticationToken createPasswordToken(ServletRequest request) throws Exception {
        Map<String, String> map = RequestResponseUtil.getRequestBodyMap(request);
        String account = map.get("account");
        String timestamp = map.get("timestamp");
        String password = map.get("password");
        String host = ServletRequestUtils.host(request);
        String userKey = map.get("userKey");
        String dynamicKey = DynamicKeyCacheUtils.get(redisTemplate, host, userKey);
        return new PasswordAuthenticationToken(account, password, timestamp, host, dynamicKey);
    }
}

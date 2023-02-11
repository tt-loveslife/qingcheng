package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.LoginLog;
import com.qingcheng.service.system.LoginLogService;
import com.qingcheng.util.WebUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

    @Reference
    private LoginLogService loginLogService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException, ServletException {
        LoginLog loginLog = new LoginLog();
        loginLog.setIp(httpServletRequest.getRemoteAddr());
        loginLog.setLoginName(authentication.getName());
        loginLog.setLoginTime(new Date());
        loginLog.setLocation(WebUtil.getCityByIP(httpServletRequest.getRemoteAddr()));
        loginLog.setBrowserName(WebUtil.getBrowserName(httpServletRequest.getHeader("user-agent")));
        httpServletRequest.getRequestDispatcher("/main.html").forward(httpServletRequest, httpServletResponse);
    }
}

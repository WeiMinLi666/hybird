package org.wyman.infrastructure.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.wyman.infrastructure.dao.po.AuthenticationRequestPO;

import java.util.List;

/**
 * 身份验证请求Mapper接口
 */
@Mapper
public interface AuthenticationRequestMapper {

    /**
     * 插入认证请求
     */
    int insert(AuthenticationRequestPO request);

    /**
     * 更新认证请求
     */
    int update(AuthenticationRequestPO request);

    /**
     * 根据请求ID查询
     */
    AuthenticationRequestPO selectById(@Param("requestId") String requestId);

    /**
     * 根据申请者ID查询
     */
    List<AuthenticationRequestPO> selectByApplicantId(@Param("applicantId") String applicantId);
}

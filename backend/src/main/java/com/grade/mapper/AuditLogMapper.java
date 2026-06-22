package com.grade.mapper;

import com.grade.entity.AuditLog;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AuditLogMapper {

    @Insert("INSERT INTO t_audit_log (operator_id, op_type, entity_type, entity_id, " +
            "old_value, new_value, op_ip, remark) VALUES " +
            "(#{operatorId}, #{opType}, #{entityType}, #{entityId}, " +
            "#{oldValue}, #{newValue}, #{opIp}, #{remark})")
    int insert(AuditLog log);

    @Select("<script>" +
            "SELECT * FROM t_audit_log WHERE 1=1 " +
            "<if test='start != null'>AND create_time &gt;= #{start}</if> " +
            "<if test='end != null'>AND create_time &lt;= #{end}</if> " +
            "<if test='opType != null'>AND op_type = #{opType}</if> " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{size}" +
            "</script>")
    List<AuditLog> selectByFilter(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  @Param("opType") String opType,
                                  @Param("offset") int offset,
                                  @Param("size") int size);
}

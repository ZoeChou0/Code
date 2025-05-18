package com.zsh.petsystem.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

  @Override
  public void insertFill(MetaObject metaObject) {

    log.info("start insert fill ....");

    if (metaObject.hasSetter("createdAt")) {
      this.strictInsertFill(metaObject, "createdAt", LocalDateTime::now, LocalDateTime.class); // 推荐使用 strictInsertFill
      // 或者 this.setFieldValByName("createdAt", LocalDateTime.now(), metaObject);
    }

    if (metaObject.hasSetter("updatedAt")) {
      this.strictInsertFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class);
    }
  }

  @Override
  public void updateFill(MetaObject metaObject) {
    log.info("start update fill ....");

    if (metaObject.hasSetter("updatedAt")) {
      this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class);
    }
  }
}

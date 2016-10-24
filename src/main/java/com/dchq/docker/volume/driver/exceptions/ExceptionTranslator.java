/**
 * COPYRIGHT (C) 2016 HyperGrid. All Rights Reserved.
 * <p>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.dchq.docker.volume.driver.exceptions;

import com.dchq.docker.volume.driver.dto.BaseResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author Intesar Mohammed
 */
@Aspect
@Component
public class ExceptionTranslator {

    final Logger logger = LoggerFactory.getLogger(getClass());

    public ExceptionTranslator() {
        logger.info("initialized");
    }

    @Around("bean(*Controller)")
    public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Object retVal = pjp.proceed();
            return retVal;
        } catch (DCHQRuntimeException e) {
            String logId = UUID.randomUUID().toString();
            logger.warn("ERROR-ID [{}]", logId);
            logger.warn(e.getLocalizedMessage());
            return new BaseResponse().withErr(e.getLocalizedMessage());
        } catch (Exception e) {
            String logId = UUID.randomUUID().toString();
            logger.warn("ERROR-ID [{}]", logId);
            logger.warn(e.getLocalizedMessage(), e);
            return new BaseResponse().withErr(e.getLocalizedMessage());
        }

    }

}

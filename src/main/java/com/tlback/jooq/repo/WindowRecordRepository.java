package com.tlback.jooq.repo;

import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class WindowRecordRepository {
    private final DSLContext dsl;


}

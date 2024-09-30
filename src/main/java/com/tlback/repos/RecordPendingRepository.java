package com.tlback.repos;

import com.tlback.domain.RecordPending;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RecordPendingRepository extends JpaRepository<RecordPending, Long> {
}

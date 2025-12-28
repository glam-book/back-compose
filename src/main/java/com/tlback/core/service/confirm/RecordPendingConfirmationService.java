package com.tlback.core.service.confirm;

import org.springframework.stereotype.Service;

@Service
public class RecordPendingConfirmationService {

    public void confirm(long recPendingId, boolean isConfirmed) {
        System.out.println("-------- ::: ----- confirm: " + recPendingId + " " + isConfirmed);
    }
    
}

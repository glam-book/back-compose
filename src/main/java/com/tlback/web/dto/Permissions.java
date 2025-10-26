package com.tlback.web.dto;

import com.tlback.core.abac.PermissionMask.Rights;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Permissions {
    
    private boolean canRead;
    private boolean canWrite;
    private boolean otherCanRead;
    private boolean otherCanWrite;

    public Permissions(Rights rights) {
        this.canRead = rights.canOwnerRead();
        this.canWrite = rights.canOwnerWrite();
        this.otherCanRead = rights.canOtherRead();
        this.otherCanWrite = rights.canOtherWrite();
    }
}

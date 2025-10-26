package com.tlback.core.abac;

import lombok.Getter;

public class PermissionMask {

    public static enum Def {
        READ(0), WRITE(1);

        public static final int OFFSET = 2;

        @Getter
        private final int maskValue;

        private Def(int maskValue) {
            this.maskValue = maskValue;
        }
    }

    public static Rights getRights(byte[] mask) {
        if (mask.length < 2)
            throw new IllegalArgumentException("Mask must be at least 2 bytes long");
        return new Rights(mask);
    }

    public static record Rights(byte[] mask) {

        public boolean canRead(boolean isOwner) {
            return isOwner ? canOwnerRead() : canOtherRead();
        }

        public boolean canWrite(boolean isOwner) {
            return isOwner ? canOwnerWrite() : canOtherWrite();
        }

        public boolean canOwnerRead() {
            return (mask[0] & (1 << Def.READ.getMaskValue())) != 0;
        }

        public boolean canOwnerWrite() {
            return (mask[0] & (1 << Def.WRITE.getMaskValue())) != 0;
        }

        public boolean canOtherRead() {
            return (mask[1] & (1 << Def.READ.getMaskValue())) != 0;
        }

        public boolean canOtherWrite() {
            return (mask[1] & (1 << Def.WRITE.getMaskValue())) != 0;
        }
    }
}

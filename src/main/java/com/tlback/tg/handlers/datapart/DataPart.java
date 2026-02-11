package com.tlback.tg.handlers.datapart;

public interface DataPart {
        String prefix();

        String convert();

        DataPart parse(String data);

}

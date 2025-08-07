package com.pdfreader.freecodedfapp.helper;

public class DataUpdatedEvent {

    public static class DevicePDFStaredEvent {
    }

    public static class PdfRenameEvent {
    }

    public static class PermanetlyDeleteEvent {
    }

    public static class RecentPDFStaredEvent {
    }

    public static class RecentPdfClearEvent {
    }

    public static class RecentPdfDeleteEvent {
    }

    public static class RecentPdfInsert {
    }

    public static class SortListEvent {
    }

    public static class ToggleGridViewEvent {
    }

    public static class PDFStaredEvent {
        public final String source;

        public PDFStaredEvent(String str) {
            this.source = str;
        }
    }
}

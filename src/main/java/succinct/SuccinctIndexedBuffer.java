package succinct;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SuccinctIndexedBuffer extends SuccinctBuffer {

    protected static char RECORD_DELIM = '\n';
    protected long[] offsets;

    public SuccinctIndexedBuffer(byte[] input, long[] offsets, int contextLen) {
        super(input, contextLen);
        this.offsets = offsets;
    }

    public SuccinctIndexedBuffer(byte[] input, long[] offsets) {
        this(input, offsets, 3);
    }

    public static char getRecordDelim() {
        return RECORD_DELIM;
    }

    private int searchOffset(long i) {
        int sp = 0, ep = offsets.length - 1;
        int m;

        while (sp <= ep) {
            m = (sp + ep) / 2;
            if (offsets[m] == i) {
                return m;
            } else if (i < offsets[m]) {
                ep = m - 1;
            } else {
                sp = m + 1;
            }
        }

        return ep;
    }

    public Long[] recordSearchOffsets(String query) {
        Set<Long> results = new HashSet<Long>();
        char[] p = query.toCharArray();
        Pair<Long, Long> range;
        range = getRange(p);

        long sp = range.first, ep = range.second;
        if (ep - sp + 1 <= 0) {
            return new Long[0];
        }

        for (long i = 0; i < ep - sp + 1; i++) {
            results.add(offsets[searchOffset(lookupSA(sp + i))]);
        }

        return results.toArray(new Long[results.size()]);
    }

    public String[] searchRecords(String query) {
        Set<Long> offsetResults = new HashSet<Long>();
        ArrayList<String> results = new ArrayList<String>();
        char[] p = query.toCharArray();
        Pair<Long, Long> range;
        range = getRange(p);

        long sp = range.first, ep = range.second;
        if (ep - sp + 1 <= 0) {
            return new String[0];
        }

        for (long i = 0; i < ep - sp + 1; i++) {
            long saVal = lookupSA(sp + i);
            int offsetIdx = searchOffset(saVal);
            long offset = offsets[offsetIdx];
            if(!offsetResults.contains(offset)) {
                results.add(extractUntil((int) offset, RECORD_DELIM));
                offsetResults.add(offset);
            }

        }

        return results.toArray(new String[results.size()]);
    }

    public long recordCount(String query) {
        return recordSearchOffsets(query).length;
    }

    public String[] extractRecords(int offset, int length) {
        String[] records = new String[offsets.length];
        for(int i = 0; i < records.length; i++) {
            long curOffset = offsets[i] + offset;
            long nextOffset = (i == records.length - 1) ? getOriginalSize() : offsets[i + 1];
            if(length < nextOffset - curOffset)
                records[i] = extract((int) curOffset, length);
            else
                records[i] = extractUntil((int) curOffset, RECORD_DELIM);
        }
        return records;
    }
}
package py.com.opentech.drawerwithbottomnavigation.utils;

import android.net.Uri;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    private static final long K = 1024;
    private static final long M = K * K;
    private static final long G = M * K;
    private static final long T = G * K;


    public static String convertToStringRepresentation(final long value) {
        final long[] dividers = new long[]{T, G, M, K, 1};
        final String[] units = new String[]{"TB", "GB", "MB", "KB", "B"};
        if (value < 1){
            return "0B";
        }
        String result = null;
        for (int i = 0; i < dividers.length; i++) {
            final long divider = dividers[i];
            if (value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
        }
        return result;
    }

    private static String format(final long value,
                                 final long divider,
                                 final String unit) {
        final double result =
                divider > 1 ? (double) value / (double) divider : (double) value;
        return new DecimalFormat("#,##0.#").format(result) + " " + unit;
    }

    public static String formatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy");

        String result = format.format(date);
        return result;
    }

    public static String getFileName(Uri uri) {
        try {
            String path = uri.getLastPathSegment();
            return path != null ? path.substring(path.lastIndexOf("/") + 1) : "unknown";

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "unknown";
    }
}

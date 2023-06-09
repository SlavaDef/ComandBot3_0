package org.dto;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.Banks.Monobank;
import org.functionalinterfase.BanksUtil;
import org.jsoup.Jsoup;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

public class MonobankService implements BanksUtil {
    long start = 0;
    String response;
    private static final String URL = "https://api.monobank.ua/bank/currency";

    @Override
    public String getCurrency(String command, DecimalFormat df) {
        try {
            if ((start == 0) || (start + 60000 < System.currentTimeMillis())) {
                response = Jsoup.connect(URL).ignoreContentType(true).get().body().text();
                start = System.currentTimeMillis();
            }
            List<Monobank> responseDtos = convertResponseToList(response);
            return responseDtos
                    .stream()
                    .filter(dto -> dto.getCurrencyCodeA() == Currency.getInstance(command)
                            .getNumericCode() && dto.getCurrencyCodeB() == 980)
                    .map(dto -> "Monobank: " + Currency.getInstance(command) +
                            " buy = " + df.format(dto.getRateBuy())
                            + " sell " + df.format(dto.getRateSell()))
                    .collect(Collectors.joining());
        } catch (IllegalArgumentException e) {
            return "такої валюти немає";
        } catch (IOException e) {
            return "Проблеми на сервері";
        }
    }

    private List<Monobank> convertResponseToList(String response) {
        Type type = TypeToken.getParameterized(List.class, Monobank.class).getType();
        Gson gson = new Gson();
        return gson.fromJson(response, type);
    }
}

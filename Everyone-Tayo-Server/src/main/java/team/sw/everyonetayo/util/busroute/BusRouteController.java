package team.sw.everyonetayo.util.busroute;

import org.json.JSONArray;
import org.json.XML;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class BusRouteController {

    @Autowired
    BusRouteRepository busRouteRepository;

    @GetMapping("/toJsonBusRoute/toJsonBusStop/{cityCode}")
    public List<RouteDto> callApiWithJson(@PathVariable String cityCode) {

        List<RouteDto> routeDtoList = new ArrayList<>();
        StringBuffer result = new StringBuffer();
        StringBuilder jsonPrintString = new StringBuilder();
        int page = 0;
        try {
            boolean next = true;
            while (next) {
                page++;
                String apiUrl = "http://openapi.tago.go.kr/openapi/service/BusRouteInfoInqireService/getRouteNoList?"
                        + "ServiceKey=tO6fJs7AxOJ%2Bf9N5nWEgSE16%2BuOewB1LlIMM%2Fs5NB6bHtZ%2B3iO%2BcOIKgzK4QrYfZmIzh0iwJ1XKdbhxKEK2FtA%3D%3D"
                        + "&cityCode=" + cityCode
                        + "&pageNo=" + page;
                URL url = new URL(apiUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                BufferedInputStream bufferedInputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream, "UTF-8"));
                String returnLine;
                while ((returnLine = bufferedReader.readLine()) != null) {
                    result.append(returnLine);
                }

                JSONObject jsonObject = XML.toJSONObject(result.toString());
                result.setLength(0);

                JSONObject responseObject = (JSONObject) jsonObject.get("response");
                JSONObject bodyObject = (JSONObject) responseObject.get("body");

                Object count = bodyObject.get("numOfRows");
                if (count.equals(0)) {
                    next = false;
                }
                JSONObject itemObject = (JSONObject) bodyObject.get("items");
                jsonPrintString.append(itemObject.toString());
                System.out.println(jsonPrintString);

                JSONArray item = (JSONArray) itemObject.get("item");
                for (int i = 0; i < item.length(); i++) {
                    JSONObject targetItem = (JSONObject) item.get(i);
                    RouteDto routeDto;

                    if (!targetItem.isNull("startvehicletime")) {
                        routeDto = new RouteDto.RouteDtoBuilder()
                                .routeTp(String.valueOf(targetItem.get("routetp")))
                                .routeId(String.valueOf(targetItem.get("routeid")))
                                .startVehicleTime(String.valueOf(targetItem.get("startvehicletime")))
                                .endVehicleTime(String.valueOf(targetItem.get("endvehicletime")))
                                .startNodeNm(String.valueOf(targetItem.get("startnodenm")))
                                .endNodeNm(String.valueOf(targetItem.get("endnodenm")))
                                .routeNo(String.valueOf(targetItem.get("routeno")))
                                .build();
                    } else {
                        routeDto = new RouteDto.RouteDtoBuilder()
                                .routeTp(String.valueOf(targetItem.get("routetp")))
                                .routeId(String.valueOf(targetItem.get("routeid")))
                                .startNodeNm(String.valueOf(targetItem.get("startnodenm")))
                                .endNodeNm(String.valueOf(targetItem.get("endnodenm")))
                                .routeNo(String.valueOf(targetItem.get("routeno")))
                                .build();
                    }

                    Route route = new Route.RouteBuilder()
                            .routeTp(String.valueOf(routeDto.getRouteTp()))
                            .routeId(String.valueOf(routeDto.getRouteId()))
                            .startVehicleTime(String.valueOf(routeDto.getStartVehicleTime()))
                            .endVehicleTime(String.valueOf(routeDto.getEndVehicleTime()))
                            .startNodeNm(String.valueOf(routeDto.getStartNodeNm()))
                            .endNodeNm(String.valueOf(routeDto.getEndNodeNm()))
                            .routeNo(String.valueOf(routeDto.getRouteNo()))
                            .cityCode(cityCode)
                            .build();

                    routeDtoList.add(routeDto);
                    busRouteRepository.save(route);
                }

                System.out.println("count = " + count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return routeDtoList;
    }
}

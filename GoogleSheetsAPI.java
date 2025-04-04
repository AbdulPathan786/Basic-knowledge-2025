public with sharing class GoogleSheetsAPI {
    
    @InvocableMethod(label = 'Google Sheet API' description = 'Fetch stock data from Google Sheet')
	public static void fetchAndUpsertStockData(List<String> sheetName) {         
        String sheetId = '1E-5GTu15vG6JXh1Cqb1nfohJ2vsojF2g76mpbrp7hM4';
        String sName = sheetName[0];
        String apiKey = 'AIzaSyAjw-IFPmw7kWC6mu3tap2cc1i-5DnjE44';
        String url = 'https://sheets.googleapis.com/v4/spreadsheets/' + sheetId + '/values/'+sName+'?key=' + apiKey;

        https://maps.googleapis.com/maps/api/js?key=AIzaSyAjw-IFPmw7kWC6mu3tap2cc1i-5DnjE44&libraries=places

        https://sheets.googleapis.com/v4/spreadsheets/1E-5GTu15vG6JXh1Cqb1nfohJ2vsojF2g76mpbrp7hM4/values/NSE?key=AIzaSyAjw-IFPmw7kWC6mu3tap2cc1i-5DnjE44

        
        Http http = new Http();
        HttpRequest request = new HttpRequest();
        request.setEndpoint(url);
        request.setMethod('GET');

        HttpResponse response = http.send(request);
        
        if (response.getStatusCode() == 200) {
            String jsonData = response.getBody();
            List<Stock__c> stockRecords = parseJsonToStock(jsonData);
            if (!stockRecords.isEmpty()) {
                upsert stockRecords Symbol__c; // Upsert using External ID
            }
        } else {
            System.debug('Error: ' + response.getStatus() + ' ' + response.getBody());
        }
    }

    private static List<Stock__c> parseJsonToStock(String jsonData) {
        List<Stock__c> stockList = new List<Stock__c>();
        Map<String, Object> jsonMap = (Map<String, Object>) JSON.deserializeUntyped(jsonData);
        
        if (jsonMap.containsKey('values')) {
            List<Object> values = (List<Object>) jsonMap.get('values');
            if (values.isEmpty()) return stockList;

            // Convert first row to headers
            List<Object> headerRow = (List<Object>) values[0];
            List<String> headers = new List<String>();
            for (Object header : headerRow) {
                headers.add(String.valueOf(header)); // Convert Object to String
            }

            // Process remaining rows
            for (Integer i = 1; i < values.size(); i++) {
                List<Object> row = (List<Object>) values[i];
                Stock__c stock = new Stock__c();
                
                for (Integer j = 0; j < headers.size(); j++) {
                    String columnName = headers[j].trim();
                    String value = (j < row.size()) ? String.valueOf(row[j]).trim() : '';
					if (columnName == 'Symbol') stock.Symbol__c = value;
                    if (columnName == 'Stock Name') stock.Name = value;
                    if (columnName == 'Price') {
                        stock.Closing_Price__c = isValidDecimal(value) ? Decimal.valueOf(value) : null;
                    }
                    /*if (columnName == 'Closing Price') {
                        stock.Yesterday_Price__c = isValidDecimal(value) ? Decimal.valueOf(value) : null;
                    }*/
                }
                
                stockList.add(stock);
            }
        }
        return stockList;
    }

    private static Boolean isValidDecimal(String value) {
        try {
            Decimal d = Decimal.valueOf(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
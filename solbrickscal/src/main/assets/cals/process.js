


    function convertTimestamp(gcalendar,timestamp,arrayId,dateformat){

                var calendar = $.calendars.instance(gcalendar);
                var date = calendar.parseDate(calendar.TIMESTAMP,timestamp);
                if(dateformat == ""){
                    dateformat = 'dd/mm/yyyy';
                }else{
                    dateformat = dateformat.replace('d','dd');
                    dateformat = dateformat.replace('m','mm');
                    dateformat = dateformat.replace('Y','yyyy');
                }
                //alert(arrayId+":"+date.formatDate(dateformat));
                alert(date.formatDate(dateformat));
                //window.AndroidApp.receiveString(date.formatDate(dateformat));
    }



function getWeekDays(gcalendar) {

    var calendar = $.calendars.instance(gcalendar);
    var weekDays = [];
    for (dayNumber = 0; dayNumber < 7; dayNumber++) {
        weekDays[dayNumber] = calendar.local.dayNames[dayNumber];
    }

    Android.processDateResult(weekDays);

}


function getDays(gcalendar,lDay) {
    var formatType = "dd-mm-yyyy";
    var calendar = $.calendars.instance(gcalendar);
    var days = [];
    var nxDate = "";
    var tdDate = "";
    var ptdDate = "";

    if(lDay == ""){
        tdDate =  calendar.formatDate(formatType, calendar.today());
    }
    var amount = 1;
    var period = "d";

        for (dayNumber = 0; dayNumber < 30; dayNumber++) {
            if(dayNumber == 0){
                 if(lDay == ""){
                    days[dayNumber] = tdDate;
                  }else{
                    ptdDate = calendar.parseDate(formatType, lDay );
                    ptdDate.add(amount, period);
                    days[dayNumber] = calendar.formatDate(formatType, ptdDate);
                  }
            }else{
                ptdDate = calendar.parseDate(formatType, days[dayNumber - 1] );
                ptdDate.add(amount, period);


                days[dayNumber] = calendar.formatDate(formatType, ptdDate);
            }
        }

    Android.processDateResult(days);
}

function getToday(gcalendar) {
    var formatType = "dd-mm-yyyy";
    var calendar = $.calendars.instance(gcalendar);
    var tdDate =  calendar.formatDate(formatType, calendar.today());
    Android.processDateResult(tdDate);
}





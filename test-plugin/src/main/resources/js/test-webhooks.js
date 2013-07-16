(function($) {
    $.extend(WebHooks, {
        initialize: function($el) { },
        render: function($el, model) {
            $("#death-star-parameters").val(model.get("parameters"));
            $(model.get("events")).each(function() {
                var event = this;
                $($("#webhook-events-list").find("[data-event-type="+event+"]")).attr("checked", true);
            });
        },
        getParameters: function() {
            return $("#death-star-parameters").val();
        },
        getEvents: function() {
            var events = [];
            $("#webhook-events-list").find("input:checked").each(function() {
                events.push($(this).attr("data-event-type"));
            });
            events.push("death-star-destroyed-event");
            return events;
        },
        reset: function() {
            $("death-star-parameters").empty();
            $("#webhook-events-list").find("input").each(function() {
                $(this).removeAttr("checked");
            });
        }
    });

})(AJS.$);
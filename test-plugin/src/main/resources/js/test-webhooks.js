(function($) {
    $.extend(WebHooks, {
        initialize: function($el, selectionModel) {
            var self = this;
            this.$el = $el;

            this.selectionModel = selectionModel;
            this.selectionModel.onSelectionChange(function (selectionModel, selectedModel) {
                this.selectedModel = selectedModel;
            }, this);
        },
        getFormattedDateTime: function(dateTime) { },
        render: function() {
            var self = this;
            this.$el.find("#death-star-parameters").val(this.selectedModel.get("parameters"));
            $(this.selectedModel.get("events")).each(function() {
                var event = this;
                self.$el.find("#webhook-events-list").find("[data-event-type="+event+"]").prop("checked", true);
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
            events.push("death_star_destroyed_event");
            return events;
        },
        reset: function() {
            $("death-star-parameters").empty();
            $("#webhook-events-list").find("input").prop("checked", false);
        },
        submitSuccess: function(model, response) { },
        submitError: function(model, response) { }
    });

})(AJS.$);

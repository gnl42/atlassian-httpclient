(function() {
	var TEMPLATES = atl.plugins.webhooks.admin.templates;
    // filled in on page-load from data attributes
    var ALL_EVENTS = null;
    var NO_EVENTS = null;
    var EVENT_MAPPING = null;

    // Global object which should be extended by product-specific implementations of webhooks
    WebHooks = {
        initialize: function($el, selectionModel) { },
        getFormattedDateTime: function(dateTime) { },
        render: function() { },
        getParameters: function() { },
        getEvents: function() { },
        reset: function() { },
        submitSuccess: function(model, response) { },
        submitError: function(model, response) { }
    };

    var WebHookModel = Backbone.Model.extend({
		url: function() { return this.get("self") || this.collection.url; },
		defaults: {
			name:AJS.I18n.getText('webhooks.add.newwebhookname'),
			url:'http://example.com/rest/webhooks/webhook1',
			lastUpdatedUser: '',
            lastUpdatedDisplayName: '',
			events:[],
			enabled: true,
            parameters: {}
		},
		idAttribute: "self"
	});

	var WebHooksCollection = Backbone.Collection.extend({
		url: AJS.contextPath() + '/rest/webhooks/1.0/webhook',
		model: WebHookModel,
		comparator: function(left, right) { return left.get("name").localeCompare(right.get("name"))},
		initialize: function() {
			this.on("change:name", function() {this.sort()}, this)
		}
	});

	var SelectionModel = Backbone.Model.extend({
		select: function(model) {
			!this.isLocked() && this.set("selection", model);
		},
		getSelected: function() {
			return this.get("selection");
		},
		lock: function() {
			this.set("locked", true);
		},
		unlock: function() {
			this.unset("locked");
		},
		isLocked: function() {
			return this.has("locked");
		},
		onSelectionChange: function(callback, that) { this.on("change:selection", callback, that) },
		onLockChange: function(callback, that) { this.on("change:locked", callback, that) }
	});

	var WebHookRow = Backbone.View.extend({
		tagName: 'tr',
		events: {
			"click" : "rowClicked"
		},
		initialize: function() {
			this.selectionModel = this.options.selectionModel;
			this.selectionModel.onSelectionChange(this.selectionChanged, this);
			this.selectionModel.onLockChange(this.lockChanged, this);
			this.model.on("remove", this.modelRemoved, this);
			this.model.on("change", this.change, this);
			_.bindAll(this, "deleteWebhook");
		},
		render: function() {
			this.$el.html(TEMPLATES.webhookRow(this.model.attributes));
			this.$el.toggleClass("webhook-disabled", !this.model.get("enabled"));

			this.$name = this.$el.find('.webhook-row-name');
			this.$url = this.$el.find('.webhook-row-url');
			this.$operationsList = this.$el.find(".operations-list");
			var content = this.$el.find(".webhook-operations-list");
			this.$el.find(".webhook-operation-delete").click(this.deleteWebhook); // can't bind via events as Dropdown moves elements
			this.selectionChanged(this.selectionModel, this.selectionModel.getSelected()); // handle event handlers reorder
			this.lockChanged(this.selectionModel, this.selectionModel.isLocked());
			return this;
		},
		change: function() {
			this.$name.text(this.model.get("name"));
			this.$url.text(this.model.get("url")).attr("href", this.model.get("url"));
			this.$el.toggleClass("webhook-disabled", !this.model.get("enabled"));
		},
		rowClicked: function() {
			this.selectionModel.select(this.model);
		},
		selectionChanged: function(selectionModel, selectedModel) {
			this.$el.toggleClass("highlighted", selectedModel === this.model);
		},
		lockChanged: function (selectionModel, lock) {
			this.$operationsList.toggleClass("hidden", !!lock);
		},
		deleteWebhook: function () {
			this.model && deleteWebhook(this.model);
		},
		modelRemoved: function() {
			this.destroy();
			this.remove();
		},
		destroy: function() {
			this.undelegateEvents();
			this.selectionModel.off(null, null, this);
			this.model.off(null, null, this);
		}
	});

	var WebHookDetailsView = Backbone.View.extend({
		el: ".webhook-details",
        events: {
            "submit form" : "submit",
            "click #webhook-edit" : "editMode",
            "click #webhook-delete" : "deleteWebhook",
            "click #webhook-disable" : "toggleEnable",
            "click #webhook-cancel" : "cancel"
        },
		initialize: function () {
			var $el = this.$el;
			this.$form = $el.find("form");
			this.$name = $el.find("#webhook-name");
			this.$nameDisplay = $el.find("#webhook-name-display");
			this.$url = $el.find("#webhook-url");
			this.$urlDisplay = $el.find("#webhook-url-display");
            this.$editedBy = $el.find("#webhook-edited-by-container");
            this.$editedDate = $el.find("#webhook-edited-date-container");
			this.$toggleEnablement = $el.find("#webhook-disable");

			this.selectedModel = undefined;
			this.selectionModel = this.model;
            WebHooks.initialize(this.$el, this.selectionModel); // doing it here so product specific code can subscribe to the events early
            this.selectionModel.onSelectionChange(this.selectionChanged, this);

			this.$form.on("cancel", function() {return false}); // so dirty form warning works after cancel
        },
		render: function() {
			this.$form.find('.error').empty();
			this.$el.find('.buttons-container').toggle(!!this.selectedModel);
			if (this.selectedModel) {
				var model = this.selectedModel;
				this.$name.val(model.get("name"));
				this.$nameDisplay.text(model.get("name"));
				this.$url.val(model.get("url"));
				this.$urlDisplay.text(model.get("url")).attr({href: model.get("url")});

                var lastUpdatedUserName = model.get("lastUpdatedUser");
                this.$editedBy.html(TEMPLATES.renderLastUpdatedUser({lastUpdatedUser: lastUpdatedUserName, lastUpdatedDisplayName: model.get("lastUpdatedDisplayName")}));
                this.$editedBy.closest('.field-group').toggle(!!lastUpdatedUserName);

                var rawUpdatedDate = model.get("lastUpdated");
                var updatedDate = new Date(rawUpdatedDate);
                var updatedDateString = WebHooks.getFormattedDateTime(updatedDate) || (updatedDate .toLocaleDateString() + " " + updatedDate .toLocaleTimeString());
                // Each product should display date formatted according to user's preferences
                this.$editedDate.html(updatedDateString);
                this.$editedDate.closest('.field-group').toggle(!!rawUpdatedDate);


                if (model.get("enabled")) {
					this.$toggleEnablement.text(AJS.I18n.getText('webhooks.disable'));
				} else {
					this.$toggleEnablement.text(AJS.I18n.getText('webhooks.enable'));
				}
                WebHooks.render(this.$el, model);

				this.$form.find('#webhook-submit').val(model.isNew() ?
						AJS.I18n.getText('webhooks.create') :
						AJS.I18n.getText('webhooks.save'));
				this.$el.find('input.text, textarea').each(function(idx, el) {el.defaultValue = el.value});

                this.$el.find("#webhook-global-message").empty();
			} else {
				this.$name.val("");
				this.$nameDisplay.text("");
				this.$url.val("");
				this.$urlDisplay.text("");
                WebHooks.reset();
			}
		},
		editMode: function () {
			this.selectionModel.lock();
			this.$form.addClass("display-mode-edit").removeClass("display-mode-display");
		},
		displayMode: function() {
			this.selectionModel.unlock();
			this.$form.addClass("display-mode-display").removeClass("display-mode-edit");
		},
		toggleEnable: function () {
			var isEnabled = this.selectedModel.get("enabled"),
					that = this;
			AJS.$.ajax({
						type: "PUT",
						dataType: "text",
						url: this.selectedModel.url() + '/enabled',
						data: !isEnabled + "",
						success: function(data) {
							that.selectedModel.set("enabled", data == "true");
						},
						error: function(xhr, status, error) {
							var actionName;
							if (isEnabled) {
								actionName = "disable";
							} else {
								actionName = "enable";
							}
							switch (xhr.status) {
								case 0:
								    displayErrorMessage(AJS.I18n.getText("webhooks.enablement.failed.server.down", actionName));
                                    break;
                            	case 404:
                            		displayErrorMessage(AJS.I18n.getText("webhooks.enablement.failed.notfound", actionName));
                            		break;
                            	default:
                            		displayErrorMessage(AJS.I18n.getText("webhooks.enablement.failed.unknownreason", actionName));
							}
						}
					}
			);
			return false;
		},
		submit: function (evt) {
			this.$form.find('.error').empty();
			var that = this;
            var wasNew = this.selectedModel.isNew();
            var parameters = WebHooks.getParameters();

            if (!_.isString(parameters)) {
                parameters = JSON.stringify(parameters);
            }

            this.selectedModel.save({
				name: this.$name.val(),
				url: this.$url.val(),
				events: WebHooks.getEvents(),
                parameters: parameters,
			}, {wait:true, success: submitSuccess, error: submitError});

			return false; // so dirty form warning works after first submit

			function submitSuccess(model, response) {
				var successMessage;
				if (wasNew) {
					successMessage = AJS.I18n.getText("webhooks.create.success", model.escape("name"));
				} else {
					successMessage = AJS.I18n.getText("webhooks.update.success", model.escape("name"));
				}
				displaySuccessMessage(successMessage);
				that.displayMode();
                WebHooks.submitSuccess.apply(WebHooks, arguments);
			}
			function submitError(model, response) {
				try {
					var errorObject = AJS.$.parseJSON(response.responseText);
				} catch (parseError) {
					errorObject = {};
				}
				switch (response.status) {
					case 400:
						var fieldErrors = errorObject.errors || {};
//						that.$name.siblings(".error").text(fieldErrors.name || "");
//						that.$url.siblings(".error").text(fieldErrors.url || "");
						break;
					case 409:
						var message = errorObject.errorMessage || "Resource conflict";
						AJS.messages.error(that.$form.find("#webhook-global-message"), {
							title: AJS.I18n.getText("webhooks.submit.duplicate.title"),
							body: "<p>" + AJS.escapeHtml(message) + "</p>",
							closeable: false});
						break;
					case 404:
						displayErrorMessage(AJS.I18n.getText("webhooks.submit.notfound"));
						break;
					default:
						displayErrorMessage(AJS.I18n.getText("webhooks.submit.error", response.status, response.statusText));
				}
                WebHooks.submitError.apply(WebHooks, arguments);
			}
		},
		cancel: function () {
			this.displayMode();
			if (this.selectedModel.isNew()) {
				this.selectedModel.destroy();
			} else {
				this.render();
			}
		},
		deleteWebhook: function() {
			this.selectedModel && deleteWebhook(this.selectedModel);
		},

		selectionChanged: function (selectionModel, selectedModel) {
			if (this.selectedModel) {
				this.selectedModel.off(null, null, this); // unbind any listeners created by this view
			}

			this.selectedModel = selectedModel;

			if (this.selectedModel) {
				this.selectedModel.on("change", this.render, this);
				selectedModel.isNew() && this.editMode();
			}

			this.render();
		}

	});

	var WebHooksTable = Backbone.View.extend({
		tagName: 'tbody',
		initialize: function() {
			this.model.on('add', this.modelAdded, this);
			this.model.on('reset', this.reset, this);
			this.$rowsCollection = [];
		},
		reset: function() {
			this.$el.empty();
			_.each(this.$rowsCollection, function(webHookRow) { webHookRow.destroy() });
			this.$rowsCollection = [];

			var that = this;
			this.model.each(function (model) {
				var webHookRow = new WebHookRow({model: model, selectionModel: that.options.selectionModel});
				that.$el.append(webHookRow.render().$el);
				that.$rowsCollection.push(webHookRow);
			});
		},
		render: function() {
			return this;
		},
		modelAdded: function(model, collection, options) {
			var webHookRow = new WebHookRow({model: model, selectionModel: this.options.selectionModel});
			this.$el.prepend(webHookRow.render().$el);
			this.$rowsCollection.push(webHookRow);
		}
	});

	var AddWebhookButton = Backbone.View.extend({
		events: { "click": "addWebhook" },
		initialize: function() { this.model.onLockChange(this.render, this) },
		render: function () { this.$el.toggleClass("disabled", this.model.isLocked()) },
		addWebhook:function () {
			!this.model.isLocked() && this.options.webHooksModel.add({/* defaults*/}, {at:0});
			return false;
		}
	});

	var GlobalPageView = Backbone.View.extend({
		el: ".webhooks",
		initialize: function() { this.model.on('reset add remove', this.countChanged, this)},
		countChanged: function(collection) {
			var empty = this.model.length == 0;
			this.$el.find(".on-webhooks-absent").toggleClass("hidden", !empty);
			this.$el.find(".on-webhooks-present").toggleClass("hidden", empty);
		}
	});

	var webHooksModel = new WebHooksCollection;
	var selectionModel = new SelectionModel;

	webHooksModel.on('remove', function (removedModel) {
		if (selectionModel.getSelected() === removedModel) {
			selectionModel.select(webHooksModel.at(0));
		}
	});
	webHooksModel.on('add', function (model, collection, options) {
		selectionModel.select(model);
	});

	AJS.$(function () {
        EVENT_MAPPING = AJS.$(".webhooks").data("event-name-mappings");
		new AddWebhookButton({model: selectionModel, el: "#add-webhook", webHooksModel: webHooksModel});
		new WebHookDetailsView({model: selectionModel}).render();
		AJS.$("table.event-webhooks").append(new WebHooksTable({model: webHooksModel, selectionModel: selectionModel}).render().$el);
		new GlobalPageView({model: webHooksModel});

		webHooksModel.fetch({
			error: function(model, response) {
				displayErrorMessage(AJS.I18n.getText("webhooks.fetch.error", response.status, response.statusText))
			},
			success: function(model) { selectionModel.select(model.at(0))}
		});

        AJS.$("#webhook-submit, #webhook-edit").click(function() {
           AJS.$("#webhook-global-message").empty();
        });
	});

	function deleteWebhook(model) {
		if (selectionModel.isLocked()) return;
		var popup = new AJS.Dialog();
		function destroyModel() {
			popup.remove();
			model.destroy({
				wait: true,
				success: function (model, response) {
					displaySuccessMessage(AJS.I18n.getText("webhooks.delete.success", model.escape("name")));
				},
				error : function (model, response) {
					if (response.status == 409) {
						var errorResponse = AJS.$.parseJSON(response.responseText);
						displayErrorMessage(errorResponse.errorMessages.join("<br>"));
					} else {
						displayErrorMessage(AJS.I18n.getText("webhooks.delete.error", model.escape("name"), response.status, response.statusText));
					}
				}
			});
		}
		popup.addHeader(AJS.I18n.getText('webhooks.delete.title'))
            .addPanel("warning-message", aui.message.info({content: AJS.I18n.getText('webhooks.delete.confirm', model.escape("name"))}))
            .addButton(AJS.I18n.getText('webhooks.delete'), destroyModel, "aui-button")
            .addCancel(AJS.I18n.getText('webhooks.cancel'), function() {popup.remove()})
            .show()
            .updateHeight();
	}
	function displaySuccessMessage(message) {
        AJS.messages.success(AJS.$("#webhook-global-message"), {
            body: AJS.escapeHtml(message),
            closeable: true
        });
	}
	function displayErrorMessage(message) {
        AJS.messages.error(AJS.$("#webhook-global-message"), {
            body: AJS.escapeHtml(message),
            closeable: true
        });
	}
})();
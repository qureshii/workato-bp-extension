{
  title: 'Blue Prism',
  secure_tunnel: true,

  connection: {
    fields: [{ name: 'profile', hint: 'On-prem Blueprism connection profile' }],
    authorization: { type: 'none'},
    apply: ->() {
      headers('X-Workato-Connector': 'enforce')
    }
  },

  test: ->(connection) {
    get("http://localhost:300/ext/#{connection['profile']}/api/v1/ping").headers('X-Workato-Connector': 'enforce')
  },
 object_definitions: {
   request_payload: {
     fields: -> (connection)   {
       [
           {
       "name": "payload",
       "type": "array",
       "typeOf": "object",
       "optional": true,
       "properties": [
         {
           "name": "index",
           "label": "Index",
           "optional": false,
           type: "integer"
         },
         {
           "name": "arg",
           "label": "Command Argument",
           "optional": false
         }
       ]
     }
       ]
   
     } },
   response_payload: {
     fields: -> (connection) {
       [
         {
           name: 'status',
           lable: 'Status',
           type: 'boolean'
         },
         
         {
           name: 'exitCode',
           lable: 'Exit Code',
           type: "number"
         },
           {
           name: 'errorMessage',
           lable: 'Error Message'
         },
          {
           name: 'output',
           lable: 'Script Output',
           type: "array",
           typeOf: "string"
         }
       ]
     }
   }
 },
  actions: {
    run_process: {
      title: 'Run Process',
      input_fields: -> { [{ name: 'run' , label: 'Run', optional: false},{ name: 'resource', label: 'Resource', optional: false}, { name: 'startp', label: 'Start Point', optional: false }] },
 

      execute: ->(connection, input,object_definations) {
        post("http://localhost/ext/#{connection['profile']}/api/v1/run-process", input).headers('X-Workato-Connector': 'enforce')
      },
      output_fields: lambda do |object_definition|
        object_definition['response_payload'] 
    
      end
    },   
    custom_process_action: {
      title: 'Custom Action',
           input_fields: -> ( obj_def) {
          obj_def['request_payload']
       },
 

      execute: ->(connection, input,object_definations) {
        post("http://localhost/ext/#{connection['profile']}/api/v1/generic-actions", input['payload']).headers('X-Workato-Connector': 'enforce')
      },
      output_fields: lambda do |object_definition|
        object_definition['response_payload'] 
    
      end
    },
    get_status: {
      title: 'Get Status',
      input_fields: -> { [{ name: 'session_id' , label: 'Session Id', optional: false}] },
 

      execute: ->(connection, input,object_definations) {
        get("http://localhost/ext/#{connection['profile']}/api/v1/status/#{input['session_id']}").headers('X-Workato-Connector': 'enforce')
      },
      output_fields: lambda do |object_definition|
        object_definition['response_payload'] 
    
      end
    },

  }


}
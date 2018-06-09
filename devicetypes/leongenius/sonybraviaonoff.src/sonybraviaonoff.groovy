/**
 *  SonyBraviaOnOff
 *
 *  Copyright 2018 Leon Zhou
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "SonyBraviaOnOff", namespace: "leongenius", author: "Leon Zhou") {
		capability "Momentary"
	}


	preferences {
        input("deviceHost", "text", title: "IP/hostname", description: "Device IP address", required: true)
        input("devicePort", "text", title: "Port", description: "Port", required: true)        
        input("devicePsk", "text", title: "PSK", description: "PSK", required: true)
    }
	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		standardTile("switch", "device.switch", width: 3, height: 4, canChangeIcon: true, decoration: "flat") {
			state "off", label: 'Push', action: "momentary.push", icon:"st.Electronics.electronics13", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Push', action: "momentary.push", icon:"st.Electronics.electronics13", backgroundColor: "#53a7c0"
		}
		main "switch"
		details "switch"
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"

}

// handle commands
def push() {
	log.debug "Executing 'push'"
    sendEvent(name: "switch", value: "on", isStateChange: true, display: false)
    sendEvent(name: "switch", value: "off", isStateChange: true, display: false)
    sendEvent(name: "momentary", value: "pushed", isStateChange: true)

    def sonycmd = new physicalgraph.device.HubSoapAction(
            path:    '/sony/IRCC',
            urn:     "urn:schemas-sony-com:service:IRCC:1",
            action:  "X_SendIRCC",
            body:    [
            	"IRCCCode": "AAAAAQAAAAEAAAAVAw=="
            ],
            headers: [
            	Host : "${deviceHost}:${devicePort}", 
                'X-Auth-PSK':"${devicePsk}"
            ]
     )
	sendHubCommand(sonycmd)
    log.debug "SonyBraviaOnOff pushed"
}
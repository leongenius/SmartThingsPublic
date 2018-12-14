/**
 *  Garage Controller
 *
 *  Copyright 2017 Leon Zhou
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
definition(
    name: "Garage Controller",
    namespace: "leongenius",
    author: "Leon Zhou",
    description: "Control my garage doors",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Door config") {
        input("theDoor", "capability.contactSensor", required: true, title: "Door sensor")
        input("theSwitch", "capability.momentary", required: true, title: "Door switch")
        input("theCar", "capability.presenceSensor", required: true, title: "Car sensor")
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(theCar, "presence", presenceHandler);
    subscribe(theDoor, "status.garage-open", doorOpenedHandler);
    subscribe(theDoor, "status.garage-closed", doorClosedHandler);
    subscribe(theDoor, "status", defaultEventHandler);
    state.ready = true;
    state.doorClosedByMe = false;
}

def presenceHandler(evt) {
    log.debug("presenceHandler called: ${evt}");
    def doorStatus = theDoor.currentState("status")?.value;
    def carPresence = theCar.currentState("presence")?.value;

    log.debug("presenceHandler doorStatus=${doorStatus}, carPresence=${carPresence}, doorClosedByMe=${state.doorClosedByMe}");
    if (!isReady()) {
    	log.debug("Door or switch is not ready.");
        getReadyInOneMinute();
        return;
    }
    if (carPresence == "present" && doorStatus == "garage-closed") {
        log.debug("presenceHandler: open door now");
        theSwitch.push();
        state.doorClosedByMe = false;
        getReadyInOneMinute();
    } else if (carPresence == "not present" && doorStatus == "garage-open") {
    	log.debug("presenceHandler: close door now");
        theSwitch.push();
        state.doorClosedByMe = true;
        getReadyInOneMinute();
    } else {
    	state.doorClosedByMe = false;
    }
}

def doorOpenedHandler(evt) {
	log.debug("doorOpenedHandler: ${evt}");
    getReadyInOneMinute();
    return;
}

def doorClosedHandler(evt) {
	log.debug("doorClosedHandler: ${evt}");
    getReadyInOneMinute();
    return;
}

def defaultEventHandler(evt) {
	log.debug("defaultEventHandler: event.name=${evt.name}, event.value=${evt.value}");
}

def getReadyInOneMinute() {
	if (state.nextReadyMs != null && state.nextReadyMs > now()) {
    	// already in waiting-for-ready status; do nothing
        def readyIn = (state.nextReadyMs - now()) / 1000;
        log.debug("Will be ready in ${readyIn} seconds");
        return;
    }
    log.debug("Set state.ready=false and get ready in 60 seconds");
	state.ready = false;
    state.nextReadyMs = now() + 60 * 1000;
    runIn(60, getReady);
}

def getReady() {
	state.ready = true;
}

def isReady() {
	return state.ready == true;
}

import { createSandboxController } from './controller.js';
import { createDomView } from './dom-view.js';
import { createWebAudioPlayer } from './web-audio-player.js';

const view = createDomView(document);
const player = createWebAudioPlayer();
const controller = createSandboxController({ view, player });

controller.init();

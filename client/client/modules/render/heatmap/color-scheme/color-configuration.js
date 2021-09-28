import * as colors from './colors';
import * as helpers from './helpers';
import {HeatmapDataType} from '../heatmap-data';

const {ColorFormats} = helpers;

/**
 * @typedef {Object} ColorConfigurationOptions
 * @property {number} [_color]
 * @property {number|string} [color=0xffffff]
 * @property {number|string} [_from=0]
 * @property {number|string} [_to=1]
 * @property {number|string} [from=0]
 * @property {number|string} [to=1]
 * @property {string} [colorFormat=number]
 * @property {HeatmapDataType} [dataType=HeatmapDataType.number]
 * @property {function} validate
 */

export default class ColorConfiguration {
    static uniqueIdentifier = 0;

    /**
     *
     * @param {ColorConfigurationOptions} options
     */
    constructor(options = {}) {
        const {
            _color,
            color = colors.white,
            dataType = HeatmapDataType.number,
            _from = (dataType === HeatmapDataType.number ? 0 : undefined),
            _to = (dataType === HeatmapDataType.number ? 1 : undefined),
            from = _from,
            to = _to,
            colorFormat = ColorFormats.number,
            validate = (() => {})
        } = options;
        ColorConfiguration.uniqueIdentifier += 1;
        this._uid = ColorConfiguration.uniqueIdentifier;
        this._color = _color !== undefined ? _color : helpers.systemColorValue(color);
        this._from = from;
        this._to = to;
        this._dataType = dataType;
        /**
         * @type {undefined|string}
         */
        this.error = undefined;
        /**
         * Color format
         * @type {ColorFormats}
         */
        this.colorFormat = colorFormat;
        this._validateAll = validate;
    }

    get uid() {
        return this._uid;
    }

    get color() {
        return helpers.formatColor(this._color, this.colorFormat);
    }

    get colorValue() {
        return this._color;
    }

    set color(color) {
        this._color = helpers.systemColorValue(color);
    }

    get value() {
        return this._from;
    }

    set value(value) {
        this._from = value;
        this._to = value;
        this.validate();
        if (typeof this._validateAll === 'function') {
            this._validateAll();
        }
    }

    get from() {
        return this._from;
    }

    set from(from) {
        this._from = from;
        this.validate();
        if (typeof this._validateAll === 'function') {
            this._validateAll();
        }
    }

    get to() {
        return this._to;
    }

    set to(to) {
        this._to = to;
        this.validate();
        if (typeof this._validateAll === 'function') {
            this._validateAll();
        }
    }

    get dataType() {
        return this._dataType;
    }

    set dataType(dataType) {
        if (this._dataType !== dataType) {
            this._dataType = dataType;
            if (this._dataType === HeatmapDataType.number) {
                this._from = !Number.isNaN(Number(this._from)) ? Number(this._from) : 0;
                this._to = !Number.isNaN(Number(this._to)) ? Number(this._to) : 1;
                this.validate();
                if (typeof this._validateAll === 'function') {
                    this._validateAll();
                }
            }
        }
    }

    copy (options = {}) {
        return new ColorConfiguration({
            color: this.color,
            colorFormat: this.colorFormat,
            from: this._from,
            to: this._to,
            dataType: this.dataType,
            validate: this._validateAll,
            ...options
        });
    }

    /**
     * Checks if color configuration is valid
     * @returns {boolean}
     */
    validate() {
        this.error = undefined;
        if (this.dataType === HeatmapDataType.number) {
            if (Number.isNaN(Number(this._from)) || Number.isNaN(Number(this._to))) {
                this.error = 'Wrong values';
            } else if (Number(this._from) > Number(this._to)) {
                this.error = '"From" value must be less or equal than "To" value';
            }
        }
        return !this.error;
    }
}

import React, { Component } from "react";
import './ServiceForm.css'
var xhr;
class ServiceForm extends Component {

  constructor(props) {
    super(props);

    this.handleChangeUrl = this.handleChangeUrl.bind(this);
    this.handleChangeName = this.handleChangeName.bind(this);
    this.changeState = this.changeState.bind(this);
    this.addService = this.addService.bind(this);
    this.processRequest = this.processRequest.bind(this);

    this.state = {url: '', name: ''};
  }

  addService() {
    if (this.state.url == '' || this.state.name == '') {
      console.log("URL and name must not be empty");
    } else {
      xhr = new XMLHttpRequest();
      xhr.open("POST", "/service")
      xhr.send(JSON.stringify({"url": this.state.url, "name": this.state.name}));
      xhr.addEventListener("readystatechange", this.processRequest, false);
    }
  }

  processRequest() {
    if (xhr.status === 200) {
      //this.props.eventDispatcher.dispatch("addService", "")
      this.changeState({url: '', name: ''})
      console.log(this.state)
    }
  }

  handleChangeUrl(event) {
    this.changeState({url: event.target.value})
  }

  handleChangeName(event) {
    this.changeState({name: event.target.value})
  }

  changeState(keyVal) {
    this.setState(Object.assign({}, this.state, keyVal))
  }

  render() {
    return (
      <>
        <form className="service-form" onSubmit={this.addService}>
          <span className="service-form-element">
            <label>URL&nbsp;
              <input type="text" value={this.state.url} onChange={this.handleChangeUrl} />
            </label>
          </span>
          <span className="service-form-element">
            <label>Name&nbsp;
              <input type="text" value={this.state.name} onChange={this.handleChangeName} />
            </label>
          </span>
          <span className="movie-form-element">
            <input type="submit" value="Add Service"/>
          </span>
        </form>
      </>
    )
  }
}

export default ServiceForm;

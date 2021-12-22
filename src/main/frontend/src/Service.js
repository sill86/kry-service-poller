import React, { Component } from "react";
import './Service.css'
var xhr;
class Service extends Component {

  constructor(props) {
    super(props);
    this.toService = this.toService.bind(this);
    this.state = {
      service: []
    }
    this.sendRequest = this.sendRequest.bind(this);
    this.deleteService = this.deleteService.bind(this);
    this.updateService = this.updateService.bind(this);
    this.handleChangeName = this.handleChangeName.bind(this);
    this.processRequest = this.processRequest.bind(this);
    this.processUpdateRequest = this.processUpdateRequest.bind(this);
    //this.props.eventDispatcher.subscribe("deleteService", this.sendRequest);
  }

  handleChangeName(e) {
    let index = e.target.getAttribute("index");
    let services = this.state.service;
    services[index].service.name = e.target.value;

    this.setState({
      service: this.state.service
    })
  }

  componentDidMount() {
    this.sendRequest()
    this.interval = setInterval(() => this.sendRequest(), 12000);
  }

  componentWillUnmount() {
    clearInterval(this.interval);
  }

  sendRequest() {
    xhr = new XMLHttpRequest();
    xhr.open("GET", "/service")
    xhr.send();
    xhr.addEventListener("readystatechange", this.processRequest, false);
  }

  deleteService(e){
    xhr = new XMLHttpRequest();
    xhr.open("DELETE", "/service")
    let url_to_delete = e.target.getAttribute("name");
    xhr.send(JSON.stringify({"url": url_to_delete}));
    xhr.addEventListener("readystatechange", this.processUpdateRequest, false);
  }

  updateService(e) {
    let index = e.target.getAttribute("index");
    let services = this.state.service;
    let url_to_update = services[index].service.url;
    let name_to_update = services[index].service.name;

    xhr = new XMLHttpRequest();
    xhr.open("PUT", "/service")
    xhr.send(JSON.stringify({"url": url_to_update, "name": name_to_update}));
    xhr.addEventListener("readystatechange", this.processUpdateRequest, false);
  }

  processRequest() {
    if (xhr.readyState === 4 && xhr.status === 200) {
      var response = JSON.parse(xhr.responseText);
      this.setState({
        service: response
      })
    }
  }

  processUpdateRequest() {
    if (xhr.readyState === 4 && xhr.status === 204) {
      this.sendRequest()
      //this.props.eventDispatcher.dispatch("deletService", "")
    }
  }

  toService(m, index) {
    return (<tbody key={m.service.url}><tr>
      <td>{m.service.url}</td>
      <td>
        <input type="text" index={index} value={m.service.name} onChange={this.handleChangeName} />
      </td>
      <td>{m.service.creationDate}</td>
      <td>{m.service.status}</td>
      <td><span><button className="floated" index={index} onClick={this.updateService}>Update</button></span>
        <span><button className="floated" name={m.service.url} onClick={this.deleteService}>Delete</button></span>
      </td>
    </tr></tbody>)
  }

  render() {
    return (
      <table className="service-list" >
        <tbody>
        <tr>
          <th>URL</th>
          <th>NAME</th>
          <th>CREATION DATE</th>
          <th>STATUS</th>
          <th></th>
        </tr>
        </tbody>
        {this.state.service.map(this.toService)}
      </table>
    )
  }
}
export default Service;

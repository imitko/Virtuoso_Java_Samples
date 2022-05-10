--  
--  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
--  project.
--  
--  Copyright (C) 1998-2021 OpenLink Software
--  
--  This project is free software; you can redistribute it and/or modify it
--  under the terms of the GNU General Public License as published by the
--  Free Software Foundation; only version 2 of the License, dated June 1991.
--  
--  This program is distributed in the hope that it will be useful, but
--  WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
--  General Public License for more details.
--  
--  You should have received a copy of the GNU General Public License along
--  with this program; if not, write to the Free Software Foundation, Inc.,
--  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
--  
--  
create procedure ostat (
    in _w_id integer,
    in _d_id integer,
    in _c_id integer,
    in _c_last varchar)
{
  declare _c_first, _c_middle, _c_balance varchar; 
  declare
    _o_id, _ol_i_id, _ol_supply_w_id, _ol_quantity, _o_carrier_id, n integer; 
  declare _ol_amount float; 
  declare _ol_delivery_d, _o_entry_d varchar; 

  if (_c_id = 0)
    {
      declare namecnt integer; 

      whenever not found goto no_customer; 
      select count (*) into namecnt 
	from tpcc..customer
	where c_last = _c_last
	  and c_d_id = _d_id
	  and c_w_id = _w_id; 

      declare c_byname cursor for 
	select c_balance, c_last, c_middle, c_id
	  from tpcc..customer
	  where c_w_id = _w_id
	    and c_d_id = _d_id
	    and c_last = _c_last
	  order by c_w_id, c_d_id, c_last, c_first;

      open c_byname; 
	
      n := 0; 
      while (n <= namecnt / 2)
        {
	  fetch c_byname into _c_balance, _c_first, _c_middle, _c_id; 
	  n := n + 1; 
	}

      close c_byname; 
    }
  else
    {
      select c_balance, c_first, c_middle, c_last
	into _c_balance, _c_first, _c_middle, _c_last
	from tpcc..customer
	where c_w_id = _w_id
	  and c_d_id = _d_id
	  and c_id = _c_id;
    }

  whenever not found goto no_order; 
  select o_id, o_carrier_id, o_entry_d 
    into _o_id, _o_carrier_id, _o_entry_d
    from tpcc..orders
    where o_w_id = _w_id
      and o_d_id = _d_id
      and o_c_id = _c_id
    order by o_w_id desc, o_d_id desc, o_c_id desc, o_id desc;

  declare o_line cursor for 
    select ol_i_id, ol_supply_w_id, ol_quantity, ol_amount, ol_delivery_d
      from tpcc..order_line
      where ol_w_id = _w_id
        and ol_d_id = _d_id
	and ol_o_id = _o_id; 

  whenever not found goto lines_done; 
  open o_line;
  result_names (_ol_supply_w_id, _ol_i_id, _ol_quantity, _ol_amount,
      _ol_delivery_d);

  while (1)
    {
      fetch o_line into _ol_i_id, _ol_supply_w_id, _ol_quantity, _ol_amount,
          _ol_delivery_d; 

      result (_ol_supply_w_id, _ol_i_id, _ol_quantity, _ol_amount,
          _ol_delivery_d);
    }

lines_done:
  end_result ();
  result_names  (_c_id, _c_last, _c_first, _c_middle, _o_entry_d,
      _o_carrier_id, _c_balance, _o_id);

  result (_c_id, _c_last, _c_first, _c_middle, _o_entry_d,
      _o_carrier_id, _c_balance, _o_id);

  return; 

no_customer:
  dbg_printf ('Nocustomer %s %d.\n', _c_last, _c_id); 
  signal ('NOCUS', 'No cystomer in order status'); 

no_order:
  return 0; 
}
